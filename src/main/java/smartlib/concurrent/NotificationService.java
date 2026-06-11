package smartlib.concurrent;

public interface NotificationService {
    void sendReturnConfirmation(String memberContact, String loanId, double fineAmount);
}
