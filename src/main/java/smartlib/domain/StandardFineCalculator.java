package smartlib.domain;

import java.time.LocalDate;

public final class StandardFineCalculator implements FineCalculator {
    private static final double DAILY_FINE = 0.50;

    @Override
    public double calculateFine(Loan loan) {
        long overdueDays = loan.overdueDays(LocalDate.now());
        return overdueDays * DAILY_FINE;
    }
}
