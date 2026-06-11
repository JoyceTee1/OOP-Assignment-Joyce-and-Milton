package smartlib.modern;

public interface BorrowEventPublisher {
    void publishBookBorrowed(String loanId, String memberId, String bookId);
}
