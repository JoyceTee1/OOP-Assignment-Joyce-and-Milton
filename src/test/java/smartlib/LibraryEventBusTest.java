package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.patterns.EventType;
import smartlib.patterns.LibraryEventBus;
import smartlib.patterns.LibraryEventListener;
import smartlib.patterns.LibraryEventMessage;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryEventBusTest {
    @Test
    void listenerReceivesCorrectEventPayload() {
        LibraryEventBus eventBus = new LibraryEventBus();
        AtomicReference<LibraryEventMessage> received = new AtomicReference<>();
        eventBus.subscribe(received::set);

        LibraryEventMessage event = new LibraryEventMessage(
                EventType.FINE_IMPOSED,
                "L-901",
                "member@test.com",
                "Fine imposed: 2.00",
                Instant.now()
        );
        eventBus.publish(event);

        assertEquals(event, received.get());
    }

    @Test
    void unsubscribedListenerDoesNotReceiveFurtherEvents() {
        LibraryEventBus eventBus = new LibraryEventBus();
        AtomicReference<LibraryEventMessage> received = new AtomicReference<>();
        LibraryEventListener listener = received::set;
        eventBus.subscribe(listener);

        eventBus.publish(new LibraryEventMessage(
                EventType.BOOK_RETURNED,
                "L-1",
                "member@test.com",
                "Returned",
                Instant.now()
        ));
        eventBus.unsubscribe(listener);
        eventBus.publish(new LibraryEventMessage(
                EventType.BOOK_RETURNED,
                "L-2",
                "member@test.com",
                "Returned again",
                Instant.now()
        ));

        assertEquals("L-1", received.get().loanId());
    }

    @Test
    void exceptionFromOneListenerDoesNotStopOtherListeners() {
        LibraryEventBus eventBus = new LibraryEventBus();
        eventBus.subscribe(event -> {
            throw new IllegalStateException("Listener A failed");
        });
        AtomicBoolean listenerBExecuted = new AtomicBoolean(false);
        eventBus.subscribe(event -> listenerBExecuted.set(true));

        eventBus.publish(new LibraryEventMessage(
                EventType.RESERVATION_EXPIRED,
                "L-10",
                "member@test.com",
                "Reservation expired",
                Instant.now()
        ));

        assertTrue(listenerBExecuted.get());
    }
}
