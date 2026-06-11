package smartlib;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import smartlib.concurrent.ConcurrentInventory;
import smartlib.concurrent.DefaultEventBus;
import smartlib.concurrent.DefaultFineService;
import smartlib.concurrent.DefaultLoanService;
import smartlib.concurrent.DomainNotificationService;
import smartlib.concurrent.InventoryServiceAdapter;
import smartlib.concurrent.NotificationService;
import smartlib.concurrent.ReturnProcessor;
import smartlib.domain.CsvLoanRepository;
import smartlib.domain.EmailNotificationService;
import smartlib.domain.InMemoryBookRepository;
import smartlib.domain.InMemoryMemberRepository;
import smartlib.domain.Loan;
import smartlib.domain.LoanManagementService;
import smartlib.domain.LoanStatus;
import smartlib.domain.StandardFineCalculator;
import smartlib.patterns.AuditLogListener;
import smartlib.patterns.SmartLibPatternComposition;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReturnProcessorTest {
    private ExecutorService pool;

    @BeforeEach
    void setUp() {
        pool = Executors.newFixedThreadPool(4);
    }

    @AfterEach
    void tearDown() {
        pool.shutdownNow();
    }

    @Test
    void processReturn_completesSuccessfully() {
        ReturnProcessorFixture fixture = fixtureForLoan("L-900", "B-900", 0);

        var result = fixture.processor().processReturn("L-900").join();

        assertTrue(result.success());
        assertEquals("L-900", result.loanId());
        assertEquals(1, fixture.inventory().availableCopies("B-900"));
        assertEquals(1, fixture.processor().processedCount());
        assertEquals(0, fixture.processor().failedCount());
        assertTrue(fixture.audit().entries().stream().anyMatch(entry -> entry.contains("BOOK_RETURNED")));
    }

    @Test
    void concurrentReturnsOnSameLoan_incrementInventoryOnlyOnce() throws Exception {
        ReturnProcessorFixture fixture = fixtureForLoan("L-900", "B-900", 0);
        ExecutorService workers = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();

        try {
            Runnable returnTask = () -> {
                try {
                    start.await();
                    if (fixture.processor().processReturn("L-900").join().success()) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            for (int i = 0; i < 4; i++) {
                workers.submit(returnTask);
            }
            start.countDown();
            workers.shutdown();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS));

            assertEquals(4, successCount.get());
            assertEquals(1, fixture.inventory().availableCopies("B-900"));
            assertEquals(4, fixture.processor().processedCount());
        } finally {
            workers.shutdownNow();
        }
    }

    @Test
    void processBatch_processesEachLoan() {
        LoanManagementService management = managementService(
                new Loan("L-1", "B-1", "M-1", "m1@test.com", LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), LoanStatus.OVERDUE),
                new Loan("L-2", "B-2", "M-2", "m2@test.com", LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), LoanStatus.OVERDUE)
        );
        ReturnProcessorFixture fixture = fixture(management, Map.of("B-1", 0, "B-2", 0));

        List<smartlib.concurrent.ReturnResult> results = fixture.processor().processBatch(List.of("L-1", "L-2"));

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(smartlib.concurrent.ReturnResult::success));
        assertEquals(1, fixture.inventory().availableCopies("B-1"));
        assertEquals(1, fixture.inventory().availableCopies("B-2"));
        assertEquals(2, fixture.processor().processedCount());
    }

    @Test
    void processReturn_whenNotificationFails_countsFailureAfterPersistingReturn() {
        LoanManagementService management = managementService(
                new Loan("L-900", "B-900", "M-900", "member@test.com",
                        LocalDate.now().minusDays(20), LocalDate.now().minusDays(5), LoanStatus.OVERDUE)
        );
        ReturnProcessorFixture fixture = fixture(
                management,
                Map.of("B-900", 0),
                new NotificationService() {
                    @Override
                    public void sendReturnConfirmation(String memberContact, String loanId, double fineAmount) {
                        throw new IllegalStateException("Notification channel unavailable");
                    }
                }
        );

        var result = fixture.processor().processReturn("L-900").join();

        assertFalse(result.success());
        assertEquals(1, fixture.inventory().availableCopies("B-900"));
        assertEquals(0, fixture.processor().processedCount());
        assertEquals(1, fixture.processor().failedCount());
    }

    private ReturnProcessorFixture fixtureForLoan(String loanId, String bookId, int initialCopies) {
        Loan loan = new Loan(
                loanId,
                bookId,
                "M-900",
                "member@test.com",
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(5),
                LoanStatus.OVERDUE
        );
        return fixture(managementService(loan), Map.of(bookId, initialCopies));
    }

    private LoanManagementService managementService(Loan... loans) {
        return new LoanManagementService(
                new CsvLoanRepository(List.of(loans)),
                new InMemoryMemberRepository(),
                new InMemoryBookRepository(),
                new StandardFineCalculator()
        );
    }

    private ReturnProcessorFixture fixture(LoanManagementService management, Map<String, Integer> inventorySeed) {
        return fixture(management, inventorySeed, new DomainNotificationService(new EmailNotificationService()));
    }

    private ReturnProcessorFixture fixture(
            LoanManagementService management,
            Map<String, Integer> inventorySeed,
            NotificationService notificationService
    ) {
        ConcurrentInventory inventory = new ConcurrentInventory(inventorySeed);
        AuditLogListener audit = new AuditLogListener();
        var eventBus = SmartLibPatternComposition.wiredEventBus(
                new smartlib.patterns.DefaultLoanService(management),
                SmartLibPatternComposition.decoratedNotificationService(),
                audit
        );
        ReturnProcessor processor = new ReturnProcessor(
                new DefaultLoanService(management),
                new DefaultFineService(management),
                new InventoryServiceAdapter(inventory),
                notificationService,
                new DefaultEventBus(eventBus),
                pool
        );
        return new ReturnProcessorFixture(processor, inventory, audit);
    }

    private record ReturnProcessorFixture(ReturnProcessor processor, ConcurrentInventory inventory, AuditLogListener audit) {
    }
}
