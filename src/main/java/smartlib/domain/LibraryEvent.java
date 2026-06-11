package smartlib.domain;

import java.time.Instant;
import java.util.Objects;

public sealed interface LibraryEvent permits LibraryEvent.BookBorrowed, LibraryEvent.BookReturned, LibraryEvent.ReservationCreated {
    String eventId();

    Instant occurredAt();

    record BookBorrowed(String eventId, String loanId, String memberId, Instant occurredAt) implements LibraryEvent {
        public BookBorrowed {
            validateText(eventId, "eventId");
            validateText(loanId, "loanId");
            validateText(memberId, "memberId");
            occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        }
    }

    record BookReturned(String eventId, String loanId, String memberId, Instant occurredAt) implements LibraryEvent {
        public BookReturned {
            validateText(eventId, "eventId");
            validateText(loanId, "loanId");
            validateText(memberId, "memberId");
            occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        }
    }

    record ReservationCreated(String eventId, String reservationId, String memberId, Instant occurredAt) implements LibraryEvent {
        public ReservationCreated {
            validateText(eventId, "eventId");
            validateText(reservationId, "reservationId");
            validateText(memberId, "memberId");
            occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        }
    }

    private static void validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
