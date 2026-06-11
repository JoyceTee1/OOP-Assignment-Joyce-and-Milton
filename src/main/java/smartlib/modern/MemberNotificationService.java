package smartlib.modern;

public interface MemberNotificationService {
    void sendReservationCreated(String memberContact, String reservationId, String bookId);

    void sendPaymentReminder(String memberContact, double outstandingFine);
}
