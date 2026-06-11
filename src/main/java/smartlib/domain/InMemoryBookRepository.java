package smartlib.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryBookRepository implements Repository<Book, String> {
    private final ConcurrentMap<String, Book> store = new ConcurrentHashMap<>();

    @Override
    public Book save(Book entity) {
        store.put(entity.id(), entity);
        return entity;
    }

    @Override
    public Optional<Book> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
