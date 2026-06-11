package smartlib.patterns;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class LibraryEventBus {
    private final CopyOnWriteArrayList<LibraryEventListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(LibraryEventListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener must not be null"));
    }

    public void unsubscribe(LibraryEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(LibraryEventMessage event) {
        Objects.requireNonNull(event, "event must not be null");
        for (LibraryEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException ex) {
                System.err.println("[EVENT_BUS_ERROR] listener=" + listener.getClass().getName()
                        + ", eventType=" + event.type()
                        + ", message=" + ex.getMessage());
            }
        }
    }
}
