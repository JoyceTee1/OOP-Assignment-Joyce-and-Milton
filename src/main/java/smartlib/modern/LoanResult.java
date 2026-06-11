package smartlib.modern;

public sealed interface LoanResult permits LoanResult.Success, LoanResult.InsufficientCopies, LoanResult.MemberSuspended, LoanResult.FineExceeded {
    record Success(String loanId, String memberId, String bookId) implements LoanResult {
        public Success {
            requireText(loanId, "loanId");
            requireText(memberId, "memberId");
            requireText(bookId, "bookId");
        }
    }

    record InsufficientCopies(String memberId, String memberContact, String bookId) implements LoanResult {
        public InsufficientCopies {
            requireText(memberId, "memberId");
            requireText(memberContact, "memberContact");
            requireText(bookId, "bookId");
        }
    }

    record MemberSuspended(String memberId, String reason) implements LoanResult {
        public MemberSuspended {
            requireText(memberId, "memberId");
            requireText(reason, "reason");
        }
    }

    record FineExceeded(String memberId, String memberContact, double outstandingFine) implements LoanResult {
        public FineExceeded {
            requireText(memberId, "memberId");
            requireText(memberContact, "memberContact");
            if (outstandingFine <= 0) {
                throw new IllegalArgumentException("outstandingFine must be > 0");
            }
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
