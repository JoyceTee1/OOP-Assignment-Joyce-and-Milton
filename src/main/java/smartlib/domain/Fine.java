package smartlib.domain;

public record Fine(String fineID, Loan loan, long overdueDays, double amount, String reason, java.time.LocalDate paidDate) {
    private static final double DAILY_RATE = 0.50;

    public Fine {
        if (fineID == null || fineID.isBlank()) {
            throw new IllegalArgumentException("fineID must not be blank");
        }
        if (loan == null) {
            throw new IllegalArgumentException("loan must not be null");
        }
        if (overdueDays < 0) {
            throw new IllegalArgumentException("overdueDays must be >= 0");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
    }

    public String loanId() {
        return loan.loanID();
    }

    public static Fine fromOverdueLoan(Loan loan, long overdueDays) {
        return new Fine(
                "FINE-" + loan.loanID(),
                loan,
                overdueDays,
                overdueDays * DAILY_RATE,
                "Overdue return",
                null
        );
    }

    /** Backward-compatible factory used by existing tests. */
    public static Fine fromOverdueDays(String loanId, long overdueDays) {
        Loan placeholder = new Loan(
                loanId,
                "B-PLACEHOLDER",
                "M-PLACEHOLDER",
                "member@smartlib.test",
                java.time.LocalDate.now().minusDays(overdueDays + 1),
                java.time.LocalDate.now().minusDays(1),
                LoanStatus.OVERDUE
        );
        return fromOverdueLoan(placeholder, overdueDays);
    }
}
