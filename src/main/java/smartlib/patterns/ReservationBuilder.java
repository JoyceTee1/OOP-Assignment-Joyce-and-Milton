package smartlib.patterns;

import smartlib.domain.Book;
import smartlib.domain.Member;
import smartlib.domain.Reservation;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class ReservationBuilder {
    private static final int COLLECTION_DAYS = 3;

    private final Member member;
    private final Book book;
    private String notes = "";
    private int priority;
    private String notificationPreferences;

    public ReservationBuilder(Member member, Book book) {
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.book = Objects.requireNonNull(book, "book must not be null");
    }

    public ReservationBuilder notes(String notes) {
        this.notes = Objects.requireNonNull(notes, "notes must not be null");
        return this;
    }

    public ReservationBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public ReservationBuilder notificationPreferences(String notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
        return this;
    }

    public Reservation build() {
        if (priority < 0) {
            throw new IllegalArgumentException("priority must be >= 0");
        }

        LocalDate reservationDate = LocalDate.now();
        return new Reservation(
                UUID.randomUUID().toString(),
                book,
                member,
                reservationDate,
                reservationDate.plusDays(COLLECTION_DAYS),
                Reservation.Status.PENDING,
                notes,
                priority,
                notificationPreferences
        );
    }
}
