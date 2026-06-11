package smartlib;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartlib.patterns.AuditLogListener;
import smartlib.patterns.EventType;
import smartlib.patterns.LibraryEventMessage;
import smartlib.patterns.LoanService;
import smartlib.patterns.MemberNotificationListener;
import smartlib.patterns.NotificationService;
import smartlib.patterns.OverdueFineListener;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatternListenersTest {
    @Mock
    private LoanService loanService;

    @Mock
    private NotificationService notificationService;

    @Test
    void overdueFineListener_calculatesFineOnBookReturned() {
        when(loanService.calculateFine("L-1")).thenReturn(2.5);

        new OverdueFineListener(loanService).onEvent(event(EventType.BOOK_RETURNED, "L-1"));

        verify(loanService).calculateFine("L-1");
    }

    @Test
    void overdueFineListener_ignoresOtherEventTypes() {
        new OverdueFineListener(loanService).onEvent(event(EventType.FINE_IMPOSED, "L-1"));

        verifyNoInteractions(loanService);
    }

    @Test
    void memberNotificationListener_notifiesOnFineImposedAndReservationExpired() {
        MemberNotificationListener listener = new MemberNotificationListener(notificationService);

        listener.onEvent(event(EventType.FINE_IMPOSED, "L-2"));
        listener.onEvent(event(EventType.RESERVATION_EXPIRED, "L-3"));

        verify(notificationService, times(2)).send("member@test.com", "Fine imposed: 2.00");
    }

    @Test
    void memberNotificationListener_ignoresBookReturned() {
        new MemberNotificationListener(notificationService).onEvent(event(EventType.BOOK_RETURNED, "L-4"));

        verifyNoInteractions(notificationService);
    }

    @Test
    void auditLogListener_recordsEveryEvent() {
        AuditLogListener audit = new AuditLogListener();

        audit.onEvent(event(EventType.BOOK_RETURNED, "L-5"));
        audit.onEvent(event(EventType.RESERVATION_EXPIRED, "L-6"));

        assertEquals(2, audit.entries().size());
        assertTrue(audit.entries().get(0).contains("BOOK_RETURNED"));
        assertTrue(audit.entries().get(1).contains("RESERVATION_EXPIRED"));
    }

    private static LibraryEventMessage event(EventType type, String loanId) {
        return new LibraryEventMessage(
                type,
                loanId,
                "member@test.com",
                "Fine imposed: 2.00",
                Instant.parse("2026-05-29T12:00:00Z")
        );
    }
}
