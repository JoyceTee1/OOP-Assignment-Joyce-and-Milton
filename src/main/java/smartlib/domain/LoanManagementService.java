package smartlib.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public final class LoanManagementService {
    private final LoanRepository loanRepository;
    private final Repository<Member, String> memberRepository;
    private final Repository<Book, String> bookRepository;
    private final FineCalculator fineCalculator;

    public LoanManagementService(
            LoanRepository loanRepository,
            Repository<Member, String> memberRepository,
            Repository<Book, String> bookRepository,
            FineCalculator fineCalculator
    ) {
        this.loanRepository = Objects.requireNonNull(loanRepository, "loanRepository must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository must not be null");
        this.fineCalculator = Objects.requireNonNull(fineCalculator, "fineCalculator must not be null");
    }

    public Loan findLoan(String loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
    }

    public Optional<Fine> calculateFine(String loanId) {
        return loanRepository.findById(loanId)
                .map(loan -> {
                    if (loan.fine().isPresent()) {
                        return loan.fine().get();
                    }
                    long overdueDays = loan.overdueDays(LocalDate.now());
                    if (overdueDays <= 0) {
                        return null;
                    }
                    Fine fine = Fine.fromOverdueLoan(loan, overdueDays);
                    loan.attachFine(fine);
                    loanRepository.save(loan);
                    return fine;
                })
                .filter(Objects::nonNull);
    }

    public Loan returnLoan(String loanId) {
        Loan loan = findLoan(loanId);
        if (!loan.returned()) {
            loan.markReturned();
            loanRepository.save(loan);
        }
        return loan;
    }

    public boolean canBorrow(Member member) {
        return member.borrowingPolicy().canBorrow(member);
    }

    public Book requireBook(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
    }

    public Member requireMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
    }

    public double calculateFineAmount(Loan loan) {
        return fineCalculator.calculateFine(loan);
    }
}
