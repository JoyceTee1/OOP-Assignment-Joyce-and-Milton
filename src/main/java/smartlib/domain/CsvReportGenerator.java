package smartlib.domain;

import java.util.List;
import java.util.stream.Collectors;

public final class CsvReportGenerator implements ReportGenerator {
    @Override
    public String format() {
        return "CSV";
    }

    @Override
    public String generate(List<Loan> loans) {
        String header = "loanId,memberEmail,daysBorrowed,returned";
        String rows = loans.stream()
                .map(loan -> loan.id() + "," + loan.memberEmail() + "," + loan.daysBorrowed() + "," + loan.returned())
                .collect(Collectors.joining(System.lineSeparator()));
        return rows.isBlank() ? header : header + System.lineSeparator() + rows;
    }
}
