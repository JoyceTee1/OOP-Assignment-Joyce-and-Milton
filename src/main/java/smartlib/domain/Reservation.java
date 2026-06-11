package smartlib.domain;

import java.time.LocalDate;
import java.util.Objects;

public final class Reservation {
    public enum Status {
        PENDING,
        FULFILLED,
        EXPIRED
    }

    private final String reservationID;
    private final Book book;
    private final Member member;
    private final LocalDate reservationDate;
    private final LocalDate expiryDate;
    private final String notes;
    private final int priority;
    private final String notificationPreferences;
    private Status status;

    public Reservation(
            String reservationID,
            Book book,
            Member member,
            LocalDate reservationDate,
            LocalDate expiryDate,
            Status status,
            String notes,
            int priority,
            String notificationPreferences
    ) {
        this.reservationID = requireText(reservationID, "reservationID");
        this.book = Objects.requireNonNull(book, "book must not be null");
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.reservationDate = Objects.requireNonNull(reservationDate, "reservationDate must not be null");
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate must not be null");
        if (expiryDate.isBefore(reservationDate)) {
            throw new IllegalArgumentException("expiryDate must not be before reservationDate");
        }
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.notes = notes == null ? "" : notes;
        if (priority < 0) {
            throw new IllegalArgumentException("priority must be >= 0");
        }
        this.priority = priority;
        this.notificationPreferences = notificationPreferences;
    }

    public Reservation(String id, String memberId, String bookId, LocalDate createdOn) {
        this(
                id,
                Book.simple(bookId, "ISBN-" + bookId, "Book " + bookId, "Unknown", true),
                new Member(memberId, "Member " + memberId, memberId + "@smartlib.test", MembershipType.STANDARD),
                createdOn,
                createdOn.plusDays(3),
                Status.PENDING,
                "",
                0,
                null
        );
    }

    public String reservationID() {
        return reservationID;
    }

    public String id() {
        return reservationID;
    }

    public Book book() {
        return book;
    }

    public String bookId() {
        return book.id();
    }

    public Member member() {
        return member;
    }

    public String memberId() {
        return member.id();
    }

    public LocalDate reservationDate() {
        return reservationDate;
    }

    public LocalDate createdOn() {
        return reservationDate;
    }

    public LocalDate expiryDate() {
        return expiryDate;
    }

    public Status status() {
        return status;
    }

    public String notes() {
        return notes;
    }

    public int priority() {
        return priority;
    }

    public String notificationPreferences() {
        return notificationPreferences;
    }

    public boolean fulfilled() {
        return status == Status.FULFILLED;
    }

    public boolean isExpired(LocalDate currentDate) {
        Objects.requireNonNull(currentDate, "currentDate must not be null");
        return currentDate.isAfter(expiryDate) && status != Status.FULFILLED;
    }

    public void markFulfilled() {
        this.status = Status.FULFILLED;
    }

    public void markExpired() {
        this.status = Status.EXPIRED;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
