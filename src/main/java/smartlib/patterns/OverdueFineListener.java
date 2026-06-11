package smartlib.patterns;

import java.util.Objects;

public final class OverdueFineListener implements LibraryEventListener {
    private final LoanService loanService;

    public OverdueFineListener(LoanService loanService) {
        this.loanService = Objects.requireNonNull(loanService, "loanService must not be null");
    }

    @Override
    public void onEvent(LibraryEventMessage event) {
        if (event.type() == EventType.BOOK_RETURNED && event.loanId() != null && !event.loanId().isBlank()) {
            loanService.calculateFine(event.loanId());
        }
    }
}
