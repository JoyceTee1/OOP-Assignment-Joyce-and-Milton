package smartlib.patterns;

public final class EmailNotificationService implements NotificationService {
    @Override
    public void send(String recipientAddress, String message) {
        if (recipientAddress == null || recipientAddress.isBlank()) {
            throw new IllegalArgumentException("recipientAddress must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        System.out.println("Email sent to " + recipientAddress + ": " + message);
    }
}
