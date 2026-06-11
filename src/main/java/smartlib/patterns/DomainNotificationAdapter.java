package smartlib.patterns;

import java.util.Objects;

/**
 * Adapts the patterns {@link NotificationService} stack to the domain return-confirmation port.
 */
public final class DomainNotificationAdapter implements smartlib.domain.NotificationService {
    private final NotificationService delegate;

    public DomainNotificationAdapter(NotificationService delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public void sendReturnConfirmation(String memberEmail, String loanId, double fine) {
        delegate.send(memberEmail, String.format("Loan %s returned. Fine: %.2f", loanId, fine));
    }
}
