package smartlib.domain;

public final class PremiumPolicy implements BorrowingPolicy {
    @Override
    public int maxBooksAllowed() {
        return 10;
    }

    @Override
    public int loanDurationDays() {
        return 28;
    }

    @Override
    public double dailyFineRate() {
        return 0.50;
    }
}
