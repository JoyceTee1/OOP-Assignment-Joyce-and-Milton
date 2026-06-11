package smartlib.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryNotificationRepository implements Repository<Notification, String> {
    private final ConcurrentMap<String, Notification> store = new ConcurrentHashMap<>();

    @Override
    public Notification save(Notification entity) {
        store.put(entity.id(), entity);
        return entity;
    }

    @Override
    public Optional<Notification> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Notification> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
