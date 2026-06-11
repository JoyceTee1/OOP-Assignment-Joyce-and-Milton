package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.domain.Fine;
import smartlib.domain.Loan;
import smartlib.domain.LoanStatus;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.functional.LoanSummary;
import smartlib.functional.LoanSummaryCollector;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanSummaryCollectorTest {
    @Test
    void collector_aggregatesLoanMetrics() {
        Member member = new Member("M-1", "Alice", "alice@test.com", MembershipType.STANDARD);
        Loan loan = new Loan(
                "L-1",
                "B-1",
                "M-1",
                "alice@test.com",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 14),
                LoanStatus.ACTIVE
        );
        Fine fine = Fine.fromOverdueDays("L-1", 2);

        Map<String, Member> members = Map.of(member.id(), member);
        Map<String, Fine> fines = Map.of(fine.loanId(), fine);

        LoanSummary summary = List.of(loan).stream()
                .collect(new LoanSummaryCollector(members, fines));

        assertEquals(1, summary.count());
        assertEquals(1.0, summary.totalFines(), 0.001);
        assertEquals(LocalDate.of(2026, 1, 1), summary.latestDate());
        assertEquals(1L, summary.breakdown().get(MembershipType.STANDARD));
    }

    @Test
    void collector_parallelStreamMatchesSequential() {
        Member standard = new Member("M-1", "Alice", "alice@test.com", MembershipType.STANDARD);
        Member premium = new Member("M-2", "Bob", "bob@test.com", MembershipType.PREMIUM);
        Loan loan1 = new Loan(
                "L-1", "B-1", "M-1", "alice@test.com",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 14), LoanStatus.ACTIVE
        );
        Loan loan2 = new Loan(
                "L-2", "B-2", "M-2", "bob@test.com",
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 15), LoanStatus.ACTIVE
        );
        Fine fine1 = Fine.fromOverdueDays("L-1", 2);
        Fine fine2 = Fine.fromOverdueDays("L-2", 4);

        Map<String, Member> members = Map.of(standard.id(), standard, premium.id(), premium);
        Map<String, Fine> fines = Map.of(fine1.loanId(), fine1, fine2.loanId(), fine2);
        LoanSummaryCollector collector = new LoanSummaryCollector(members, fines);
        List<Loan> loans = List.of(loan1, loan2);

        LoanSummary sequential = loans.stream().collect(collector);
        LoanSummary parallel = loans.parallelStream().collect(collector);

        assertEquals(sequential, parallel);
        assertEquals(2, parallel.count());
        assertEquals(3.0, parallel.totalFines(), 0.001);
        assertEquals(LocalDate.of(2026, 2, 1), parallel.latestDate());
        assertEquals(1L, parallel.breakdown().get(MembershipType.STANDARD));
        assertEquals(1L, parallel.breakdown().get(MembershipType.PREMIUM));
    }
}
