package smartlib.domain;

import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    Optional<Loan> findById(String loanId);

    void save(Loan loan);

    List<Loan> findAll();
}
