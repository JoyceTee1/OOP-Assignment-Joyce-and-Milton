package smartlib.modern;

import smartlib.domain.Reservation;

public interface ReservationService {
    Reservation createReservation(String memberId, String bookId);
}
