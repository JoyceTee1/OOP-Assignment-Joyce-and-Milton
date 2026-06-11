package smartlib.patterns;

import java.util.Objects;

public final class MemberNotificationListener implements LibraryEventListener {
    private final NotificationService notificationService;

    public MemberNotificationListener(NotificationService notificationService) {
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService must not be null");
    }

    @Override
    public void onEvent(LibraryEventMessage event) {
        if (event.type() == EventType.RESERVATION_EXPIRED || event.type() == EventType.FINE_IMPOSED) {
            notificationService.send(event.memberContact(), event.details());
        }
    }
}
