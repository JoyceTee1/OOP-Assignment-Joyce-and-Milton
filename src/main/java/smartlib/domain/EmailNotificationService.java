package smartlib.domain;

public final class EmailNotificationService implements NotificationService {
    @Override
    public void sendReturnConfirmation(String memberEmail, String loanId, double fine) {
        System.out.printf("Email to %s: Loan %s returned. Fine: %.2f%n", memberEmail, loanId, fine);
    }
}
