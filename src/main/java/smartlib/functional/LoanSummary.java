package smartlib.functional;

import smartlib.domain.MembershipType;

import java.time.LocalDate;
import java.util.Map;

public record LoanSummary(int count, double totalFines, LocalDate latestDate, Map<MembershipType, Long> breakdown) {
}
