package smartlib.domain;

public interface NotificationService {
    void sendReturnConfirmation(String memberEmail, String loanId, double fine);
}
