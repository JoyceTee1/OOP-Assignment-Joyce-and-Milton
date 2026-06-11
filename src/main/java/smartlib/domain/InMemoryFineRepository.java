package smartlib.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryFineRepository implements Repository<Fine, String> {
    private final ConcurrentMap<String, Fine> store = new ConcurrentHashMap<>();

    @Override
    public Fine save(Fine entity) {
        store.put(entity.loanId(), entity);
        return entity;
    }

    @Override
    public Optional<Fine> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Fine> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
