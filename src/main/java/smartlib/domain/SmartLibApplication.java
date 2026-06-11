package smartlib.domain;

import smartlib.patterns.SmartLibPatternComposition;

import java.time.LocalDate;
import java.util.List;

public final class SmartLibApplication {
    private SmartLibApplication() {
    }

    public static LibraryService createLibraryService() {
        FineCalculator fineCalculator = new StandardFineCalculator();
        LoanRepository loanRepository = new CsvLoanRepository(List.of(
                new Loan("L-100", "B-100", "M-100", "alice@smartlib.test", LocalDate.now().minusDays(10), LocalDate.now().plusDays(4), LoanStatus.ACTIVE),
                new Loan("L-101", "B-101", "M-101", "bob@smartlib.test", LocalDate.now().minusDays(21), LocalDate.now().minusDays(7), LoanStatus.OVERDUE)
        ));
        LoanManagementService loanManagement = new LoanManagementService(
                loanRepository,
                new InMemoryMemberRepository(),
                new InMemoryBookRepository(),
                fineCalculator
        );
        NotificationService notificationService = SmartLibPatternComposition.domainNotificationService(
                SmartLibPatternComposition.decoratedNotificationService()
        );
        List<ReportGenerator> reportGenerators = List.of(
                new PdfReportGenerator(),
                new CsvReportGenerator(),
                new ConsoleReportGenerator()
        );
        return new LibraryService(
                fineCalculator,
                notificationService,
                loanRepository,
                reportGenerators,
                SmartLibPatternComposition.wiredReturnEvents(loanManagement)
        );
    }

    public static void main(String[] args) {
        LibraryService libraryService = createLibraryService();
        double fine = libraryService.returnBook("L-101");
        System.out.println("Fine charged: " + fine);
        System.out.println(libraryService.generateReport("CSV"));
    }
}
