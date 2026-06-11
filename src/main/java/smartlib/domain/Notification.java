package smartlib.domain;

import java.time.Instant;
import java.util.Objects;

public record Notification(
        String id,
        String memberId,
        NotificationChannel channel,
        String message,
        Instant createdAt
) {
    public Notification {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("memberId must not be blank");
        }
        channel = Objects.requireNonNull(channel, "channel must not be null");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
