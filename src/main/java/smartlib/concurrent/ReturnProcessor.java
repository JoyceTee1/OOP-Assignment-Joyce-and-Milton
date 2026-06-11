package smartlib.concurrent;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ReturnProcessor {
    private static final Duration RETURN_TIMEOUT = Duration.ofSeconds(30);

    private final LoanService loanService;
    private final FineService fineService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final EventBus eventBus;
    private final ExecutorService threadPool;
    private final AtomicInteger processedCount = new AtomicInteger();
    private final AtomicInteger failedCount = new AtomicInteger();
    private final ConcurrentHashMap<String, Object> loanLocks = new ConcurrentHashMap<>();

    public ReturnProcessor(
            LoanService loanService,
            FineService fineService,
            InventoryService inventoryService,
            NotificationService notificationService,
            EventBus eventBus,
            ExecutorService threadPool
    ) {
        this.loanService = Objects.requireNonNull(loanService, "loanService must not be null");
        this.fineService = Objects.requireNonNull(fineService, "fineService must not be null");
        this.inventoryService = Objects.requireNonNull(inventoryService, "inventoryService must not be null");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService must not be null");
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus must not be null");
        this.threadPool = Objects.requireNonNull(threadPool, "threadPool must not be null");
    }

    public CompletableFuture<ReturnResult> processReturn(String loanID) {
        Objects.requireNonNull(loanID, "loanID must not be null");
        if (loanID.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("loanID must not be blank"));
        }

        return CompletableFuture
                .supplyAsync(() -> processReturnAtomically(loanID), threadPool)
                .thenApplyAsync(this::completeReturnWithNotifications, threadPool)
                .orTimeout(RETURN_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                .handle((result, error) -> {
                    if (error != null) {
                        failedCount.incrementAndGet();
                        Throwable cause = unwrap(error);
                        System.err.println("[RETURN_ERROR] loanId=" + loanID + ": " + cause.getMessage());
                        return ReturnResult.failed(loanID, cause);
                    }
                    return result;
                });
    }

    public List<ReturnResult> processBatch(List<String> loanIDs) {
        Objects.requireNonNull(loanIDs, "loanIDs must not be null");
        List<CompletableFuture<ReturnResult>> futures = loanIDs.stream()
                .map(this::processReturn)
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    public int processedCount() {
        return processedCount.get();
    }

    public int failedCount() {
        return failedCount.get();
    }

    private ReturnedLoanWithFine processReturnAtomically(String loanID) {
        synchronized (lockFor(loanID)) {
            ReturnedLoan returnedLoan = loanService.returnLoan(loanID);
            double fineAmount = fineService.calculateFine(returnedLoan);
            if (returnedLoan.newlyReturned()) {
                inventoryService.markBookAvailable(returnedLoan.bookId());
            }
            return new ReturnedLoanWithFine(returnedLoan, fineAmount);
        }
    }

    private ReturnResult completeReturnWithNotifications(ReturnedLoanWithFine loanWithFine) {
        ReturnedLoan returnedLoan = loanWithFine.returnedLoan();
        notificationService.sendReturnConfirmation(
                returnedLoan.memberContact(),
                returnedLoan.loanId(),
                loanWithFine.fineAmount()
        );
        eventBus.publishBookReturned(
                returnedLoan.loanId(),
                returnedLoan.memberContact(),
                returnedLoan.bookId(),
                loanWithFine.fineAmount()
        );
        processedCount.incrementAndGet();
        return ReturnResult.success(returnedLoan.loanId(), loanWithFine.fineAmount());
    }

    private Object lockFor(String loanId) {
        return loanLocks.computeIfAbsent(loanId, id -> new Object());
    }

    private static Throwable unwrap(Throwable error) {
        if (error instanceof java.util.concurrent.CompletionException completionException
                && completionException.getCause() != null) {
            return completionException.getCause();
        }
        return error;
    }

    private record ReturnedLoanWithFine(ReturnedLoan returnedLoan, double fineAmount) {
    }
}
