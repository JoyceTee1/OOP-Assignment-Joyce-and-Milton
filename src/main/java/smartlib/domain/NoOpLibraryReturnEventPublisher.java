package smartlib.domain;

public final class NoOpLibraryReturnEventPublisher implements LibraryReturnEventPublisher {
    @Override
    public void publishBookReturned(String loanId, String memberEmail, double fineAmount) {
    }

    @Override
    public void publishFineImposed(String loanId, String memberEmail, double fineAmount) {
    }
}
