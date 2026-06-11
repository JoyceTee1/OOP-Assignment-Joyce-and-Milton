package smartlib.patterns;

public final class RetryDecorator extends NotificationDecorator {
    private final int maxAttempts;

    public RetryDecorator(NotificationService delegate, int maxAttempts) {
        super(delegate);
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be > 0");
        }
        this.maxAttempts = maxAttempts;
    }

    @Override
    public void send(String recipientAddress, String message) {
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                delegate.send(recipientAddress, message);
                return;
            } catch (RuntimeException ex) {
                lastFailure = ex;
                if (attempt == maxAttempts) {
                    break;
                }
                long retryIndex = attempt - 1L;
                long backoffMs = 100L * (1L << retryIndex);
                System.out.println("[RETRY] attempt=" + attempt + " failed, backing off " + backoffMs + "ms");
                sleep(backoffMs);
            }
        }

        throw new IllegalStateException("Notification failed after " + maxAttempts + " attempts", lastFailure);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted", e);
        }
    }
}
