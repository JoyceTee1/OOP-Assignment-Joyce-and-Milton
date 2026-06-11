package smartlib.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryLoanEntityRepository implements Repository<Loan, String> {
    private final ConcurrentMap<String, Loan> store = new ConcurrentHashMap<>();

    @Override
    public Loan save(Loan entity) {
        store.put(entity.id(), entity);
        return entity;
    }

    @Override
    public Optional<Loan> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Loan> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
