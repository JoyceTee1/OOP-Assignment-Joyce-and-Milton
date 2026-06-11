package smartlib.patterns;

import smartlib.domain.LoanManagementService;

import java.util.Objects;

public final class DefaultLoanService implements LoanService {
    private final LoanManagementService loanManagementService;

    public DefaultLoanService(LoanManagementService loanManagementService) {
        this.loanManagementService = Objects.requireNonNull(loanManagementService, "loanManagementService must not be null");
    }

    @Override
    public double calculateFine(String loanId) {
        return loanManagementService.calculateFine(loanId)
                .map(fine -> fine.amount())
                .orElse(0.0);
    }
}
