package smartlib.concurrent;

public record ReturnResult(boolean success, String loanId, double fineAmount, String message, Throwable cause) {
    public static ReturnResult success(String loanId, double fineAmount) {
        return new ReturnResult(true, loanId, fineAmount, "Return processed successfully", null);
    }

    public static ReturnResult failed(String loanId, Throwable cause) {
        String message = cause == null ? "Failed to process return" : cause.getMessage();
        return new ReturnResult(false, loanId, 0.0, message, cause);
    }
}
