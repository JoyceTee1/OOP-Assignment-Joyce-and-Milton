package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.patterns.EmailNotificationService;
import smartlib.patterns.LoggingDecorator;
import smartlib.patterns.NotificationService;
import smartlib.patterns.RateLimitingDecorator;
import smartlib.patterns.RetryDecorator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DecoratorTest {
    @Test
    void composedDecorators_sendWithoutThrowing() {
        NotificationService service = new RateLimitingDecorator(
                new RetryDecorator(new LoggingDecorator(new EmailNotificationService()), 3),
                5
        );

        assertDoesNotThrow(() -> service.send("member@test.com", "Loan returned"));
    }
}
