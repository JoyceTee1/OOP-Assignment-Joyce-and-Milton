package smartlib.patterns;

import smartlib.domain.Book;
import smartlib.domain.Loan;
import smartlib.domain.LoanStatus;
import smartlib.domain.Member;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class LoanBuilder {
    private final Member member;
    private final Book book;
    private String notes = "";
    private int renewalCount;
    private String referenceCode;

    public LoanBuilder(Member member, Book book) {
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.book = Objects.requireNonNull(book, "book must not be null");
    }

    public LoanBuilder notes(String notes) {
        this.notes = Objects.requireNonNull(notes, "notes must not be null");
        return this;
    }

    public LoanBuilder renewalCount(int renewalCount) {
        this.renewalCount = renewalCount;
        return this;
    }

    public LoanBuilder referenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
        return this;
    }

    public Loan build() {
        if (renewalCount < 0) {
            throw new IllegalArgumentException("renewalCount must be >= 0");
        }

        String loanID = UUID.randomUUID().toString();
        LocalDate loanDate = LocalDate.now();
        int duration = member.borrowingPolicy().loanDurationDays();
        LocalDate dueDate = loanDate.plusDays(duration);

        return new Loan(
                loanID,
                book,
                member,
                loanDate,
                dueDate,
                null,
                LoanStatus.ACTIVE,
                notes,
                renewalCount,
                referenceCode
        );
    }
}
