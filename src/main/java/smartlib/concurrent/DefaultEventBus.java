package smartlib.concurrent;

import smartlib.patterns.EventType;
import smartlib.patterns.LibraryEventBus;
import smartlib.patterns.LibraryEventMessage;

import java.time.Instant;
import java.util.Objects;

public final class DefaultEventBus implements EventBus {
    private final LibraryEventBus eventBus;

    public DefaultEventBus(LibraryEventBus eventBus) {
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus must not be null");
    }

    @Override
    public void publishBookReturned(String loanId, String memberContact, String bookId, double fineAmount) {
        eventBus.publish(new LibraryEventMessage(
                EventType.BOOK_RETURNED,
                loanId,
                memberContact,
                "Book " + bookId + " returned. Fine: " + fineAmount,
                Instant.now()
        ));
        if (fineAmount > 0.0) {
            eventBus.publish(new LibraryEventMessage(
                    EventType.FINE_IMPOSED,
                    loanId,
                    memberContact,
                    "Fine imposed: " + fineAmount,
                    Instant.now()
            ));
        }
    }
}
