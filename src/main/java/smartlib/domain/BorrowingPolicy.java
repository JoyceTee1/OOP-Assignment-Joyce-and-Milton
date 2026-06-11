package smartlib.domain;

public interface BorrowingPolicy {
    int maxBooksAllowed();

    int loanDurationDays();

    double dailyFineRate();

    default boolean canBorrow(Member member) {
        if (member == null) {
            return false;
        }
        if (member.outstandingFine() > 10.0) {
            return false;
        }
        return member.activeLoansCount() < maxBooksAllowed();
    }
}
