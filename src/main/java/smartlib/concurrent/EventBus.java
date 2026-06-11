package smartlib.concurrent;

public interface EventBus {
    void publishBookReturned(String loanId, String memberId, String bookId, double fineAmount);
}
