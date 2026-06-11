package smartlib.functional;

import smartlib.domain.Fine;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class LoanSummaryCollector implements Collector<Loan, LoanSummaryCollector.Accumulator, LoanSummary> {
    private final Map<String, Member> membersById;
    private final Map<String, Fine> finesByLoanId;

    public LoanSummaryCollector(Map<String, Member> membersById, Map<String, Fine> finesByLoanId) {
        this.membersById = Map.copyOf(Objects.requireNonNull(membersById, "membersById must not be null"));
        this.finesByLoanId = Map.copyOf(Objects.requireNonNull(finesByLoanId, "finesByLoanId must not be null"));
    }

    @Override
    public Supplier<Accumulator> supplier() {
        return Accumulator::new;
    }

    @Override
    public BiConsumer<Accumulator, Loan> accumulator() {
        return (acc, loan) -> {
            Objects.requireNonNull(loan, "loan must not be null");
            acc.count++;
            Fine fine = loan.fine().orElse(finesByLoanId.get(loan.id()));
            if (fine != null) {
                acc.totalFines += fine.amount();
            }
            if (acc.latestDate == null || loan.borrowedOn().isAfter(acc.latestDate)) {
                acc.latestDate = loan.borrowedOn();
            }

            Member member = membersById.get(loan.memberId());
            if (member != null) {
                acc.breakdown.merge(member.membershipType(), 1L, Long::sum);
            }
        };
    }

    @Override
    public BinaryOperator<Accumulator> combiner() {
        return (left, right) -> {
            left.count += right.count;
            left.totalFines += right.totalFines;

            if (left.latestDate == null || (right.latestDate != null && right.latestDate.isAfter(left.latestDate))) {
                left.latestDate = right.latestDate;
            }

            right.breakdown.forEach((membershipType, count) ->
                    left.breakdown.merge(membershipType, count, Long::sum));

            return left;
        };
    }

    @Override
    public Function<Accumulator, LoanSummary> finisher() {
        return acc -> new LoanSummary(
                acc.count,
                acc.totalFines,
                acc.latestDate,
                Map.copyOf(acc.breakdown)
        );
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }

    static final class Accumulator {
        private int count;
        private double totalFines;
        private LocalDate latestDate;
        private final Map<MembershipType, Long> breakdown = new EnumMap<>(MembershipType.class);
    }
}
