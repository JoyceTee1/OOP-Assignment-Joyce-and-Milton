package smartlib.domain;

/**
 * Publishes return-related domain events without tying {@link LibraryService} to a concrete observer bus.
 */
public interface LibraryReturnEventPublisher {
    void publishBookReturned(String loanId, String memberEmail, double fineAmount);

    void publishFineImposed(String loanId, String memberEmail, double fineAmount);
}
