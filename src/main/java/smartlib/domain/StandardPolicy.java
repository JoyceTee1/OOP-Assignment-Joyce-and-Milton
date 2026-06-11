package smartlib.domain;

public final class StandardPolicy implements BorrowingPolicy {
    @Override
    public int maxBooksAllowed() {
        return 3;
    }

    @Override
    public int loanDurationDays() {
        return 14;
    }

    @Override
    public double dailyFineRate() {
        return 0.50;
    }
}
