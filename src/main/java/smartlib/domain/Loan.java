package smartlib.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

public final class Loan {
    private final String loanID;
    private final Book book;
    private final Member member;
    private final LocalDate loanDate;
    private final LocalDate dueDate;
    private final String notes;
    private final int renewalCount;
    private final String referenceCode;
    private LoanStatus status;
    private LocalDate returnDate;
    private Fine fine;

    public Loan(
            String loanID,
            Book book,
            Member member,
            LocalDate loanDate,
            LocalDate dueDate,
            LocalDate returnDate,
            LoanStatus status,
            String notes,
            int renewalCount,
            String referenceCode
    ) {
        this.loanID = requireText(loanID, "loanID");
        this.book = Objects.requireNonNull(book, "book must not be null");
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.loanDate = Objects.requireNonNull(loanDate, "loanDate must not be null");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate must not be null");
        if (dueDate.isBefore(loanDate)) {
            throw new IllegalArgumentException("dueDate must not be before loanDate");
        }
        this.returnDate = returnDate;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.notes = notes == null ? "" : notes;
        if (renewalCount < 0) {
            throw new IllegalArgumentException("renewalCount must be >= 0");
        }
        this.renewalCount = renewalCount;
        this.referenceCode = referenceCode;
    }

    /** Backward-compatible constructor used by existing service wiring. */
    public Loan(
            String id,
            String bookId,
            String memberId,
            String memberEmail,
            LocalDate borrowedOn,
            LocalDate dueDate,
            LoanStatus status
    ) {
        this(
                id,
                Book.simple(bookId, "ISBN-" + bookId, "Book " + bookId, "Unknown Author", true),
                new Member(memberId, "Member " + memberId, memberEmail, MembershipType.STANDARD),
                borrowedOn,
                dueDate,
                null,
                status,
                "",
                0,
                null
        );
    }

    public String loanID() {
        return loanID;
    }

    public String id() {
        return loanID;
    }

    public Book book() {
        return book;
    }

    public String bookId() {
        return book.id();
    }

    public Member member() {
        return member;
    }

    public String memberId() {
        return member.id();
    }

    public String memberEmail() {
        return member.email();
    }

    public LocalDate loanDate() {
        return loanDate;
    }

    public LocalDate borrowedOn() {
        return loanDate;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    public LocalDate returnDate() {
        return returnDate;
    }

    public LocalDate returnedOn() {
        return returnDate;
    }

    public LoanStatus status() {
        return status;
    }

    public String notes() {
        return notes;
    }

    public int renewalCount() {
        return renewalCount;
    }

    public String referenceCode() {
        return referenceCode;
    }

    public Optional<Fine> fine() {
        return Optional.ofNullable(fine);
    }

    public void attachFine(Fine imposedFine) {
        this.fine = Objects.requireNonNull(imposedFine, "imposedFine must not be null");
    }

    public boolean returned() {
        return status == LoanStatus.RETURNED;
    }

    public long daysBorrowed() {
        LocalDate endDate = returnDate != null ? returnDate : dueDate;
        return ChronoUnit.DAYS.between(loanDate, endDate);
    }

    public long overdueDays(LocalDate currentDate) {
        if (status == LoanStatus.RETURNED && returnDate != null) {
            return Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
        }
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, currentDate));
    }

    public Loan markReturned() {
        return markReturned(LocalDate.now());
    }

    public Loan markReturned(LocalDate returnedDate) {
        this.returnDate = Objects.requireNonNull(returnedDate, "returnedDate must not be null");
        this.status = LoanStatus.RETURNED;
        return this;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
