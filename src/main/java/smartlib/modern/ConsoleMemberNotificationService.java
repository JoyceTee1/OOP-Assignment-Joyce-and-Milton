package smartlib.modern;

public final class ConsoleMemberNotificationService implements MemberNotificationService {
    @Override
    public void sendReservationCreated(String memberContact, String reservationId, String bookId) {
        System.out.printf("Reservation created for %s: id=%s, book=%s%n", memberContact, reservationId, bookId);
    }

    @Override
    public void sendPaymentReminder(String memberContact, double outstandingFine) {
        System.out.printf("Payment reminder for %s: outstanding fine $%.2f%n", memberContact, outstandingFine);
    }
}
