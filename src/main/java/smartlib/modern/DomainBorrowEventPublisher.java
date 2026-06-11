package smartlib.modern;

import smartlib.domain.LibraryEvent;
import smartlib.domain.Repository;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class DomainBorrowEventPublisher implements BorrowEventPublisher {
    private final Repository<LibraryEvent, String> eventRepository;

    public DomainBorrowEventPublisher(Repository<LibraryEvent, String> eventRepository) {
        this.eventRepository = Objects.requireNonNull(eventRepository, "eventRepository must not be null");
    }

    @Override
    public void publishBookBorrowed(String loanId, String memberId, String bookId) {
        eventRepository.save(new LibraryEvent.BookBorrowed(
                UUID.randomUUID().toString(),
                loanId,
                memberId,
                Instant.now()
        ));
    }
}
