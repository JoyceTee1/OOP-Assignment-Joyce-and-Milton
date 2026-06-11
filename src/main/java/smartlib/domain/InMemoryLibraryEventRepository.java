package smartlib.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryLibraryEventRepository implements Repository<LibraryEvent, String> {
    private final ConcurrentMap<String, LibraryEvent> store = new ConcurrentHashMap<>();

    @Override
    public LibraryEvent save(LibraryEvent entity) {
        store.put(entity.eventId(), entity);
        return entity;
    }

    @Override
    public Optional<LibraryEvent> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<LibraryEvent> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
