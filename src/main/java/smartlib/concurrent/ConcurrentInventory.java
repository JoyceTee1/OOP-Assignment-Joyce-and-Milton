package smartlib.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ConcurrentInventory {
    private final Map<String, Integer> availableByIsbn = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Condition copyAvailable = lock.writeLock().newCondition();

    public ConcurrentInventory(Map<String, Integer> initialCopies) {
        Objects.requireNonNull(initialCopies, "initialCopies must not be null");
        for (Map.Entry<String, Integer> entry : initialCopies.entrySet()) {
            String isbn = requireIsbn(entry.getKey());
            Integer count = Objects.requireNonNull(entry.getValue(), "copy count must not be null");
            if (count < 0) {
                throw new IllegalArgumentException("copy count must be >= 0");
            }
            availableByIsbn.put(isbn, count);
        }
    }

    public boolean borrow(String isbn, long timeoutMs) throws InterruptedException {
        String normalizedIsbn = requireIsbn(isbn);
        if (timeoutMs < 0) {
            throw new IllegalArgumentException("timeoutMs must be >= 0");
        }

        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        writeLock.lockInterruptibly();
        try {
            long deadline = System.currentTimeMillis() + timeoutMs;
            while (availableByIsbn.getOrDefault(normalizedIsbn, 0) <= 0) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    return false;
                }
                if (!copyAvailable.await(remaining, TimeUnit.MILLISECONDS)) {
                    return false;
                }
            }
            availableByIsbn.merge(normalizedIsbn, -1, Integer::sum);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public void returnCopy(String isbn) {
        String normalizedIsbn = requireIsbn(isbn);
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            availableByIsbn.merge(normalizedIsbn, 1, Integer::sum);
            copyAvailable.signalAll();
        } finally {
            writeLock.unlock();
        }
    }

    public int availableCopies(String isbn) {
        String normalizedIsbn = requireIsbn(isbn);
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        readLock.lock();
        try {
            return availableByIsbn.getOrDefault(normalizedIsbn, 0);
        } finally {
            readLock.unlock();
        }
    }

    private static String requireIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("isbn must not be blank");
        }
        return isbn;
    }
}
