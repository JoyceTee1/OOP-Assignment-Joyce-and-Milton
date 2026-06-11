package smartlib.concurrent;

import smartlib.domain.Loan;
import smartlib.domain.LoanManagementService;
import smartlib.domain.LoanNotFoundException;

import java.time.LocalDate;
import java.util.Objects;

public final class DefaultLoanService implements LoanService {
    private final LoanManagementService loanManagementService;

    public DefaultLoanService(LoanManagementService loanManagementService) {
        this.loanManagementService = Objects.requireNonNull(loanManagementService, "loanManagementService must not be null");
    }

    @Override
    public ReturnedLoan returnLoan(String loanId) {
        try {
            Loan existing = loanManagementService.findLoan(loanId);
            boolean newlyReturned = !existing.returned();
            Loan loan = loanManagementService.returnLoan(loanId);
            long overdueDays = loan.overdueDays(LocalDate.now());
            return new ReturnedLoan(
                    loan.loanID(),
                    loan.memberId(),
                    loan.memberEmail(),
                    loan.bookId(),
                    overdueDays,
                    newlyReturned
            );
        } catch (LoanNotFoundException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
