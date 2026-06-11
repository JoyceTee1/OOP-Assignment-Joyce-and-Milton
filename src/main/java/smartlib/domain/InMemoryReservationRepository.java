package smartlib.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryReservationRepository implements Repository<Reservation, String> {
    private final ConcurrentMap<String, Reservation> store = new ConcurrentHashMap<>();

    @Override
    public Reservation save(Reservation entity) {
        store.put(entity.id(), entity);
        return entity;
    }

    @Override
    public Optional<Reservation> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
