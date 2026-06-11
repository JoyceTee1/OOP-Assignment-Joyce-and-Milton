package smartlib.domain;

import java.util.List;

public interface ReportGenerator {
    String format();

    String generate(List<Loan> loans);
}
