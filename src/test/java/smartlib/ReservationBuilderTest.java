package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.Reservation;
import smartlib.patterns.ReservationBuilder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationBuilderTest {
    @Test
    void minimalReservation_onlyMemberAndBook_usesDefaults() {
        Member member = new Member("M-0", "Alice", "alice@test.com", MembershipType.STANDARD);
        Book book = Book.simple("B-0", "ISBN-000", "Intro Java", "Author", true);

        Reservation reservation = new ReservationBuilder(member, book).build();

        assertNotNull(reservation.reservationID());
        assertEquals(LocalDate.now(), reservation.reservationDate());
        assertEquals(LocalDate.now().plusDays(3), reservation.expiryDate());
        assertEquals("", reservation.notes());
        assertEquals(0, reservation.priority());
    }

    @Test
    void build_createsReservationWithThreeDayExpiry() {
        Member member = new Member("M-1", "Alice", "alice@test.com", MembershipType.STANDARD);
        Book book = Book.simple("B-1", "ISBN-001", "Clean Code", "Robert Martin", true);

        Reservation reservation = new ReservationBuilder(member, book)
                .notes("Hold at desk")
                .priority(1)
                .notificationPreferences("EMAIL")
                .build();

        assertNotNull(reservation.reservationID());
        assertEquals(LocalDate.now(), reservation.reservationDate());
        assertEquals(LocalDate.now().plusDays(3), reservation.expiryDate());
        assertEquals("Hold at desk", reservation.notes());
        assertEquals(1, reservation.priority());
        assertEquals("EMAIL", reservation.notificationPreferences());
    }

    @Test
    void negativePriority_throwsIllegalArgumentException() {
        Member member = new Member("M-2", "Bob", "bob@test.com", MembershipType.PREMIUM);
        Book book = Book.simple("B-2", "ISBN-002", "DDD", "Eric Evans", true);

        ReservationBuilder builder = new ReservationBuilder(member, book).priority(-1);
        assertThrows(IllegalArgumentException.class, builder::build);
    }
}
