package smartlib.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryLoanRepository implements LoanRepository {
    private final Map<String, Loan> loansById = new ConcurrentHashMap<>();

    public InMemoryLoanRepository(List<Loan> seedLoans) {
        for (Loan loan : seedLoans) {
            loansById.put(loan.id(), loan);
        }
    }

    @Override
    public Optional<Loan> findById(String loanId) {
        return Optional.ofNullable(loansById.get(loanId));
    }

    @Override
    public void save(Loan loan) {
        loansById.put(loan.id(), loan);
    }

    @Override
    public List<Loan> findAll() {
        return new ArrayList<>(loansById.values());
    }
}
