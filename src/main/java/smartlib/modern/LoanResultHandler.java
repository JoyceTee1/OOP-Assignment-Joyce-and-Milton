package smartlib.modern;

import smartlib.domain.Reservation;

import java.util.Objects;
import java.util.function.Consumer;

public final class LoanResultHandler {
    private final BorrowEventPublisher eventPublisher;
    private final ReservationService reservationService;
    private final MemberNotificationService notificationService;
    private final Consumer<String> logger;

    public LoanResultHandler(
            BorrowEventPublisher eventPublisher,
            ReservationService reservationService,
            MemberNotificationService notificationService,
            Consumer<String> logger
    ) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.reservationService = Objects.requireNonNull(reservationService, "reservationService must not be null");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService must not be null");
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
    }

    public void handle(LoanResult result) {
        switch (result) {
            case LoanResult.Success success -> {
                logger.accept("Loan created: " + success.loanId());
                eventPublisher.publishBookBorrowed(success.loanId(), success.memberId(), success.bookId());
            }
            case LoanResult.InsufficientCopies insufficient -> {
                Reservation reservation = reservationService.createReservation(
                        insufficient.memberId(),
                        insufficient.bookId()
                );
                notificationService.sendReservationCreated(
                        insufficient.memberContact(),
                        reservation.reservationID(),
                        insufficient.bookId()
                );
            }
            case LoanResult.MemberSuspended suspended ->
                    logger.accept("WARN: member " + suspended.memberId() + " is suspended. Reason: " + suspended.reason());
            case LoanResult.FineExceeded fineExceeded ->
                    notificationService.sendPaymentReminder(fineExceeded.memberContact(), fineExceeded.outstandingFine());
        }
    }
}
