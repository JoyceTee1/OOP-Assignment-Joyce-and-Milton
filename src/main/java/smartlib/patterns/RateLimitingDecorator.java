package smartlib.patterns;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RateLimitingDecorator extends NotificationDecorator {
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final int maxPerMinute;
    private final Map<String, Deque<Instant>> sendsByRecipient = new ConcurrentHashMap<>();

    public RateLimitingDecorator(NotificationService delegate, int maxPerMinute) {
        super(delegate);
        if (maxPerMinute <= 0) {
            throw new IllegalArgumentException("maxPerMinute must be > 0");
        }
        this.maxPerMinute = maxPerMinute;
    }

    @Override
    public void send(String recipientAddress, String message) {
        Instant now = Instant.now();
        Deque<Instant> sendTimes = sendsByRecipient.computeIfAbsent(recipientAddress, key -> new ArrayDeque<>());

        synchronized (sendTimes) {
            trimExpired(sendTimes, now);
            if (sendTimes.size() >= maxPerMinute) {
                System.out.println("[RATE_LIMIT] Dropped notification for recipient=" + recipientAddress + " at " + now);
                return;
            }
        }

        delegate.send(recipientAddress, message);

        synchronized (sendTimes) {
            sendTimes.addLast(Instant.now());
        }
    }

    private void trimExpired(Deque<Instant> sendTimes, Instant now) {
        while (!sendTimes.isEmpty()) {
            Instant oldest = sendTimes.peekFirst();
            if (oldest == null || oldest.plus(WINDOW).isAfter(now)) {
                return;
            }
            sendTimes.removeFirst();
        }
    }
}

