package smartlib.domain;

import java.util.List;

public final class PdfReportGenerator implements ReportGenerator {
    @Override
    public String format() {
        return "PDF";
    }

    @Override
    public String generate(List<Loan> loans) {
        return "PDF report generated for " + loans.size() + " loans";
    }
}
