package smartlib.patterns;

import java.time.Instant;
import java.util.Objects;

public record LibraryEventMessage(
        EventType type,
        String loanId,
        String memberContact,
        String details,
        Instant occurredAt
) {
    public LibraryEventMessage {
        type = Objects.requireNonNull(type, "type must not be null");
        if (memberContact == null || memberContact.isBlank()) {
            throw new IllegalArgumentException("memberContact must not be blank");
        }
        details = details == null ? "" : details;
        occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
