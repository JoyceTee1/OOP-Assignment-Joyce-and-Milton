package smartlib.domain;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class LibraryService {
    private final FineCalculator fineCalculator;
    private final NotificationService notificationService;
    private final LoanRepository loanRepository;
    private final Map<String, ReportGenerator> reportGeneratorsByFormat;
    private final LibraryReturnEventPublisher eventPublisher;

    public LibraryService(
            FineCalculator fineCalculator,
            NotificationService notificationService,
            LoanRepository loanRepository,
            List<ReportGenerator> reportGenerators
    ) {
        this(fineCalculator, notificationService, loanRepository, reportGenerators, new NoOpLibraryReturnEventPublisher());
    }

    public LibraryService(
            FineCalculator fineCalculator,
            NotificationService notificationService,
            LoanRepository loanRepository,
            List<ReportGenerator> reportGenerators,
            LibraryReturnEventPublisher eventPublisher
    ) {
        this.fineCalculator = Objects.requireNonNull(fineCalculator, "fineCalculator must not be null");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService must not be null");
        this.loanRepository = Objects.requireNonNull(loanRepository, "loanRepository must not be null");
        Objects.requireNonNull(reportGenerators, "reportGenerators must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.reportGeneratorsByFormat = reportGenerators.stream()
                .collect(Collectors.toUnmodifiableMap(
                        generator -> generator.format().toUpperCase(Locale.ROOT),
                        Function.identity()
                ));
    }

    public double returnBook(String loanId) {
        validateLoanId(loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));

        double fine = fineCalculator.calculateFine(loan);
        Loan returnedLoan = loan.markReturned();

        loanRepository.save(returnedLoan);
        notificationService.sendReturnConfirmation(returnedLoan.memberEmail(), returnedLoan.id(), fine);
        eventPublisher.publishBookReturned(returnedLoan.id(), returnedLoan.memberEmail(), fine);
        if (fine > 0.0) {
            eventPublisher.publishFineImposed(returnedLoan.id(), returnedLoan.memberEmail(), fine);
        }
        return fine;
    }

    public String generateReport(String format) {
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("format must not be blank");
        }
        ReportGenerator generator = reportGeneratorsByFormat.get(format.toUpperCase(Locale.ROOT));
        if (generator == null) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
        return generator.generate(loanRepository.findAll());
    }

    private void validateLoanId(String loanId) {
        if (loanId == null || loanId.isBlank()) {
            throw new IllegalArgumentException("loanId must not be blank");
        }
    }
}
