package smartlib.concurrent;

import java.util.Objects;

public final class InventoryServiceAdapter implements InventoryService {
    private final ConcurrentInventory inventory;

    public InventoryServiceAdapter(ConcurrentInventory inventory) {
        this.inventory = Objects.requireNonNull(inventory, "inventory must not be null");
    }

    @Override
    public void markBookAvailable(String bookId) {
        inventory.returnCopy(bookId);
    }
}
