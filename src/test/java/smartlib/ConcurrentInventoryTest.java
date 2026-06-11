package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.concurrent.ConcurrentInventory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrentInventoryTest {
    @Test
    void borrowAndReturn_updatesAvailability() throws InterruptedException {
        ConcurrentInventory inventory = new ConcurrentInventory(Map.of("ISBN-1", 1));

        assertTrue(inventory.borrow("ISBN-1", 100));
        assertEquals(0, inventory.availableCopies("ISBN-1"));

        inventory.returnCopy("ISBN-1");
        assertEquals(1, inventory.availableCopies("ISBN-1"));
    }

    @Test
    void borrow_timesOutWhenNoCopiesAvailable() throws InterruptedException {
        ConcurrentInventory inventory = new ConcurrentInventory(Map.of("ISBN-1", 0));
        assertFalse(inventory.borrow("ISBN-1", 50));
    }

    @Test
    void concurrentBorrowers_doNotDriveInventoryNegative() throws Exception {
        ConcurrentInventory inventory = new ConcurrentInventory(Map.of("ISBN-1", 1));
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();

        Runnable borrower = () -> {
            try {
                start.await();
                if (inventory.borrow("ISBN-1", 500)) {
                    successCount.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        for (int i = 0; i < 4; i++) {
            pool.submit(borrower);
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(1, successCount.get());
        assertEquals(0, inventory.availableCopies("ISBN-1"));
    }

    @Test
    void returnCopy_signalsWaitingBorrower() throws Exception {
        ConcurrentInventory inventory = new ConcurrentInventory(Map.of("ISBN-1", 0));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch borrowerStarted = new CountDownLatch(1);
        AtomicBoolean borrowed = new AtomicBoolean(false);

        pool.submit(() -> {
            borrowerStarted.countDown();
            try {
                borrowed.set(inventory.borrow("ISBN-1", 2_000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertTrue(borrowerStarted.await(2, TimeUnit.SECONDS));
        Thread.sleep(100);
        assertEquals(0, inventory.availableCopies("ISBN-1"));

        inventory.returnCopy("ISBN-1");

        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(borrowed.get());
        assertEquals(0, inventory.availableCopies("ISBN-1"));
    }
}
