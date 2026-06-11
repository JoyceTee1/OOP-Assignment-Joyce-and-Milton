package smartlib.patterns;

import java.time.Instant;

public final class LoggingDecorator extends NotificationDecorator {
    public LoggingDecorator(NotificationService delegate) {
        super(delegate);
    }

    @Override
    public void send(String recipientAddress, String message) {
        Instant now = Instant.now();
        System.out.println("[LOG] recipient=" + recipientAddress + ", message=\"" + message + "\", timestamp=" + now);
        delegate.send(recipientAddress, message);
    }
}
