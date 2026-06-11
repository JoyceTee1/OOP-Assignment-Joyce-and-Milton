package smartlib.domain;

public final class StudentPolicy implements BorrowingPolicy {
    @Override
    public int maxBooksAllowed() {
        return 5;
    }

    @Override
    public int loanDurationDays() {
        return 21;
    }

    @Override
    public double dailyFineRate() {
        return 0.50;
    }
}
