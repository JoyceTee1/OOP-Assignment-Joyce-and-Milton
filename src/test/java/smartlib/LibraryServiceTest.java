package smartlib;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartlib.domain.FineCalculator;
import smartlib.domain.LibraryService;
import smartlib.domain.Loan;
import smartlib.domain.LoanNotFoundException;
import smartlib.domain.LoanRepository;
import smartlib.domain.LoanStatus;
import smartlib.domain.NotificationService;
import smartlib.domain.ReportGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {
    @Mock
    private FineCalculator fineCalculator;

    @Mock
    private NotificationService notificationService;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ReportGenerator reportGenerator;

    @Captor
    private ArgumentCaptor<Loan> savedLoanCaptor;

    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        when(reportGenerator.format()).thenReturn("CSV");
        libraryService = new LibraryService(
                fineCalculator,
                notificationService,
                loanRepository,
                List.of(reportGenerator)
        );
    }

    @Test
    void returnBook_marksLoanReturned_savesLoan_andSendsNotification() {
        Loan activeLoan = new Loan(
                "L-200",
                "B-200",
                "M-200",
                "member@smartlib.test",
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(6),
                LoanStatus.OVERDUE
        );

        when(loanRepository.findById("L-200")).thenReturn(Optional.of(activeLoan));
        when(fineCalculator.calculateFine(activeLoan)).thenReturn(3.0);

        double result = libraryService.returnBook("L-200");

        assertEquals(3.0, result);
        verify(loanRepository).save(savedLoanCaptor.capture());
        Loan savedLoan = savedLoanCaptor.getValue();
        assertEquals("L-200", savedLoan.id());
        assertTrue(savedLoan.returned());
        verify(notificationService).sendReturnConfirmation("member@smartlib.test", "L-200", 3.0);
    }

    @Test
    void generateReport_delegatesToRegisteredGenerator() {
        when(reportGenerator.generate(List.of())).thenReturn("CSV report");
        when(loanRepository.findAll()).thenReturn(List.of());

        String report = libraryService.generateReport("CSV");

        assertEquals("CSV report", report);
        verify(reportGenerator).generate(List.of());
    }

    @Test
    void returnBook_throwsWhenLoanDoesNotExist() {
        when(loanRepository.findById("L-404")).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> libraryService.returnBook("L-404"));
    }
}
