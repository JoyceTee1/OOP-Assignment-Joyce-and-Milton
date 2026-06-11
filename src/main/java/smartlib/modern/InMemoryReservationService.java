package smartlib.modern;

import smartlib.domain.Book;
import smartlib.domain.Member;
import smartlib.domain.Repository;
import smartlib.domain.Reservation;
import smartlib.patterns.ReservationBuilder;

import java.util.Objects;

public final class InMemoryReservationService implements ReservationService {
    private final Repository<Reservation, String> reservationRepository;
    private final Repository<Member, String> memberRepository;
    private final Repository<Book, String> bookRepository;

    public InMemoryReservationService(
            Repository<Reservation, String> reservationRepository,
            Repository<Member, String> memberRepository,
            Repository<Book, String> bookRepository
    ) {
        this.reservationRepository = Objects.requireNonNull(reservationRepository, "reservationRepository must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository must not be null");
    }

    @Override
    public Reservation createReservation(String memberId, String bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
        Reservation reservation = new ReservationBuilder(member, book).build();
        return reservationRepository.save(reservation);
    }
}
