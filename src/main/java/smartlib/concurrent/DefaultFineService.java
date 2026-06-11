package smartlib.concurrent;

import smartlib.domain.Fine;
import smartlib.domain.LoanManagementService;

import java.util.Objects;

public final class DefaultFineService implements FineService {
    private final LoanManagementService loanManagementService;

    public DefaultFineService(LoanManagementService loanManagementService) {
        this.loanManagementService = Objects.requireNonNull(loanManagementService, "loanManagementService must not be null");
    }

    @Override
    public double calculateFine(ReturnedLoan returnedLoan) {
        Objects.requireNonNull(returnedLoan, "returnedLoan must not be null");
        return loanManagementService.calculateFine(returnedLoan.loanId())
                .map(Fine::amount)
                .orElse(0.0);
    }
}
