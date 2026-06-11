package smartlib.domain;

import java.util.List;

public final class ConsoleReportGenerator implements ReportGenerator {
    @Override
    public String format() {
        return "CONSOLE";
    }

    @Override
    public String generate(List<Loan> loans) {
        return loans.stream()
                .map(loan -> "Loan " + loan.id() + " | member=" + loan.memberEmail() + " | days=" + loan.daysBorrowed() + " | returned=" + loan.returned())
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("No loans to print");
    }
}
