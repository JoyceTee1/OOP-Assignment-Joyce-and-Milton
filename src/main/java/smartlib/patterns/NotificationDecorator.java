package smartlib.patterns;

import java.util.Objects;

public abstract class NotificationDecorator implements NotificationService {
    protected final NotificationService delegate;

    protected NotificationDecorator(NotificationService delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }
}
