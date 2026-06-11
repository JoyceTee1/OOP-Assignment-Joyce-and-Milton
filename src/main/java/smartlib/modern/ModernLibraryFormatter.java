package smartlib.modern;

import smartlib.domain.Book;
import smartlib.domain.BorrowingPolicy;
import smartlib.domain.Fine;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.Notification;
import smartlib.domain.PremiumPolicy;
import smartlib.domain.Reservation;
import smartlib.domain.StandardPolicy;
import smartlib.domain.StudentPolicy;

public final class ModernLibraryFormatter {
    public String describe(Object entity) {
        if (entity instanceof Book b) {
            return "Book[id=%s, title=%s, isbn=%s, available=%s]".formatted(
                    b.id(),
                    b.title(),
                    b.isbn(),
                    b.isAvailable()
            );
        }
        if (entity instanceof Member m) {
            return "Member[id=%s, name=%s, type=%s, activeLoans=%d, outstandingFine=%.2f]".formatted(
                    m.id(),
                    m.name(),
                    m.membershipType(),
                    m.activeLoansCount(),
                    m.outstandingFine()
            );
        }
        if (entity instanceof Loan l) {
            return "Loan[id=%s, memberId=%s, bookId=%s, dueDate=%s, status=%s]".formatted(
                    l.id(),
                    l.memberId(),
                    l.bookId(),
                    l.dueDate(),
                    l.status()
            );
        }
        if (entity instanceof Fine f) {
            return "Fine[loanId=%s, overdueDays=%d, amount=%.2f]".formatted(
                    f.loan().loanID(),
                    f.overdueDays(),
                    f.amount()
            );
        }
        if (entity instanceof Reservation r) {
            return "Reservation[id=%s, memberId=%s, bookId=%s, createdOn=%s, fulfilled=%s]".formatted(
                    r.id(),
                    r.memberId(),
                    r.bookId(),
                    r.createdOn(),
                    r.fulfilled()
            );
        }
        if (entity instanceof Notification n) {
            return "Notification[id=%s, memberId=%s, channel=%s, createdAt=%s]".formatted(
                    n.id(),
                    n.memberId(),
                    n.channel(),
                    n.createdAt()
            );
        }
        return "Unknown entity type: " + (entity == null ? "null" : entity.getClass().getSimpleName());
    }

    public BorrowingPolicy policyFor(MembershipType type) {
        return switch (type) {
            case STANDARD -> new StandardPolicy();
            case PREMIUM -> new PremiumPolicy();
            case STUDENT -> new StudentPolicy();
            // No default on purpose: MembershipType is an enum, so the compiler can enforce exhaustiveness.
        };
    }

    public String buildNotificationPayload(Member member, String message) {
        return """
                {
                  "memberId": "%s",
                  "memberName": "%s",
                  "membershipType": "%s",
                  "message": "%s"
                }
                """.formatted(
                member.id(),
                member.name(),
                member.membershipType(),
                message
        );
    }
}
