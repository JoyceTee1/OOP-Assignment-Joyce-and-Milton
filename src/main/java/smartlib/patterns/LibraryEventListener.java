package smartlib.patterns;

@FunctionalInterface
public interface LibraryEventListener {
    void onEvent(LibraryEventMessage event);
}
