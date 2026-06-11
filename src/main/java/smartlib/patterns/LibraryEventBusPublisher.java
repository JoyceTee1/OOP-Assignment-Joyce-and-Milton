package smartlib.patterns;

import smartlib.domain.LibraryReturnEventPublisher;

import java.time.Instant;
import java.util.Objects;

public final class LibraryEventBusPublisher implements LibraryReturnEventPublisher {
    private final LibraryEventBus eventBus;

    public LibraryEventBusPublisher(LibraryEventBus eventBus) {
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus must not be null");
    }

    @Override
    public void publishBookReturned(String loanId, String memberEmail, double fineAmount) {
        eventBus.publish(new LibraryEventMessage(
                EventType.BOOK_RETURNED,
                loanId,
                memberEmail,
                "Book returned. Fine: " + fineAmount,
                Instant.now()
        ));
    }

    @Override
    public void publishFineImposed(String loanId, String memberEmail, double fineAmount) {
        eventBus.publish(new LibraryEventMessage(
                EventType.FINE_IMPOSED,
                loanId,
                memberEmail,
                "Fine imposed: " + fineAmount,
                Instant.now()
        ));
    }
}
