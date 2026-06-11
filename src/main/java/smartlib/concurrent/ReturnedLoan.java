package smartlib.concurrent;

public record ReturnedLoan(
        String loanId,
        String memberId,
        String memberContact,
        String bookId,
        long overdueDays,
        boolean newlyReturned
) {
    public ReturnedLoan(String loanId, String memberId, String memberContact, String bookId, long overdueDays) {
        this(loanId, memberId, memberContact, bookId, overdueDays, false);
    }
}
