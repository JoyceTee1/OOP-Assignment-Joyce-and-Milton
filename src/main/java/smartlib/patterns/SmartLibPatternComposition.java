package smartlib.patterns;

import smartlib.domain.LibraryReturnEventPublisher;
import smartlib.domain.LoanManagementService;

import java.util.Objects;

/**
 * Composition root for Task 2 patterns: decorator notification stack and wired observer bus.
 */
public final class SmartLibPatternComposition {
    private SmartLibPatternComposition() {
    }

    public static NotificationService decoratedNotificationService() {
        return new RateLimitingDecorator(
                new RetryDecorator(
                        new LoggingDecorator(new EmailNotificationService()),
                        3
                ),
                5
        );
    }

    public static smartlib.domain.NotificationService domainNotificationService(NotificationService decorated) {
        return new DomainNotificationAdapter(decorated);
    }

    public static LibraryEventBus wiredEventBus(LoanService loanService, NotificationService notificationService) {
        return wiredEventBus(loanService, notificationService, new AuditLogListener());
    }

    public static LibraryEventBus wiredEventBus(
            LoanService loanService,
            NotificationService notificationService,
            AuditLogListener auditLogListener
    ) {
        Objects.requireNonNull(loanService, "loanService must not be null");
        Objects.requireNonNull(notificationService, "notificationService must not be null");
        Objects.requireNonNull(auditLogListener, "auditLogListener must not be null");

        LibraryEventBus bus = new LibraryEventBus();
        bus.subscribe(new OverdueFineListener(loanService));
        bus.subscribe(new MemberNotificationListener(notificationService));
        bus.subscribe(auditLogListener);
        return bus;
    }

    public static LibraryReturnEventPublisher eventPublisher(LibraryEventBus bus) {
        return new LibraryEventBusPublisher(bus);
    }

    public static LibraryReturnEventPublisher wiredReturnEvents(LoanManagementService loanManagement) {
        LoanService loanService = new DefaultLoanService(loanManagement);
        NotificationService notifications = decoratedNotificationService();
        LibraryEventBus bus = wiredEventBus(loanService, notifications);
        return eventPublisher(bus);
    }
}
