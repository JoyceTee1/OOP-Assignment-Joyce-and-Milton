package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.domain.CsvLoanRepository;
import smartlib.domain.InMemoryBookRepository;
import smartlib.domain.InMemoryMemberRepository;
import smartlib.domain.LibraryService;
import smartlib.domain.Loan;
import smartlib.domain.LoanManagementService;
import smartlib.domain.LoanStatus;
import smartlib.domain.StandardFineCalculator;
import smartlib.patterns.AuditLogListener;
import smartlib.patterns.SmartLibPatternComposition;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryServiceReturnEventsTest {
    @Test
    void returnBook_publishesEventsAndAuditLogWhenWired() {
        Loan overdueLoan = new Loan(
                "L-500",
                "B-500",
                "M-500",
                "member@test.com",
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(5),
                LoanStatus.OVERDUE
        );
        var loanRepository = new CsvLoanRepository(List.of(overdueLoan));
        LoanManagementService management = new LoanManagementService(
                loanRepository,
                new InMemoryMemberRepository(),
                new InMemoryBookRepository(),
                new StandardFineCalculator()
        );
        AuditLogListener audit = new AuditLogListener();
        var eventBus = SmartLibPatternComposition.wiredEventBus(
                new smartlib.patterns.DefaultLoanService(management),
                SmartLibPatternComposition.decoratedNotificationService(),
                audit
        );
        LibraryService libraryService = new LibraryService(
                new StandardFineCalculator(),
                SmartLibPatternComposition.domainNotificationService(
                        SmartLibPatternComposition.decoratedNotificationService()
                ),
                loanRepository,
                List.of(),
                SmartLibPatternComposition.eventPublisher(eventBus)
        );

        double fine = libraryService.returnBook("L-500");

        assertTrue(fine > 0.0);
        assertTrue(audit.entries().stream().anyMatch(entry -> entry.contains("BOOK_RETURNED")));
        assertTrue(audit.entries().stream().anyMatch(entry -> entry.contains("FINE_IMPOSED")));
        assertEquals(2, audit.entries().size());
    }
}
