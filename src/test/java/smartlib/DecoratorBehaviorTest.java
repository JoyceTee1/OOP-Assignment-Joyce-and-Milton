package smartlib;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartlib.patterns.LoggingDecorator;
import smartlib.patterns.NotificationService;
import smartlib.patterns.RateLimitingDecorator;
import smartlib.patterns.RetryDecorator;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DecoratorBehaviorTest {
    @Mock
    private NotificationService delegate;

    @Test
    void loggingDecorator_delegatesToWrappedService() {
        new LoggingDecorator(delegate).send("member@test.com", "Loan returned");

        verify(delegate).send("member@test.com", "Loan returned");
    }

    @Test
    void retryDecorator_retriesBeforeGivingUp() {
        AtomicInteger attempts = new AtomicInteger();
        NotificationService failing = (recipient, message) -> {
            if (attempts.incrementAndGet() < 2) {
                throw new IllegalStateException("temporary failure");
            }
        };
        NotificationService service = new RetryDecorator(failing, 3);

        service.send("member@test.com", "Retry me");

        assertEquals(2, attempts.get());
    }

    @Test
    void retryDecorator_rethrowsAfterMaxAttempts() {
        NotificationService failing = (recipient, message) -> {
            throw new IllegalStateException("SMTP unavailable");
        };

        assertThrows(IllegalStateException.class,
                () -> new RetryDecorator(failing, 2).send("member@test.com", "Retry me"));
    }

    @Test
    void rateLimitingDecorator_dropsMessagesBeyondPerMinuteCap() {
        AtomicInteger sends = new AtomicInteger();
        NotificationService inner = (recipient, message) -> sends.incrementAndGet();
        NotificationService limited = new RateLimitingDecorator(inner, 2);

        limited.send("member@test.com", "one");
        limited.send("member@test.com", "two");
        limited.send("member@test.com", "three");

        assertEquals(2, sends.get());
    }
}
