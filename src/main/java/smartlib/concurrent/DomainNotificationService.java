package smartlib.concurrent;

import java.util.Objects;

public final class DomainNotificationService implements NotificationService {
    private final smartlib.domain.NotificationService delegate;

    public DomainNotificationService(smartlib.domain.NotificationService delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public void sendReturnConfirmation(String memberContact, String loanId, double fineAmount) {
        delegate.sendReturnConfirmation(memberContact, loanId, fineAmount);
    }
}
