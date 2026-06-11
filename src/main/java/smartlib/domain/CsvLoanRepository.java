package smartlib.domain;

import java.util.List;
import java.util.Optional;

/**
 * In-memory loan repository implementing the persistence boundary required by Task 1.
 * A CSV-backed implementation can replace the delegate without changing {@link LibraryService}.
 */
public final class CsvLoanRepository implements LoanRepository {
    private final InMemoryLoanRepository delegate;

    public CsvLoanRepository(List<Loan> seedLoans) {
        this.delegate = new InMemoryLoanRepository(seedLoans);
    }

    @Override
    public Optional<Loan> findById(String loanId) {
        return delegate.findById(loanId);
    }

    @Override
    public void save(Loan loan) {
        delegate.save(loan);
    }

    @Override
    public List<Loan> findAll() {
        return delegate.findAll();
    }
}
