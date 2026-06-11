package smartlib.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AuditLogListener implements LibraryEventListener {
    private final List<String> auditEntries = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onEvent(LibraryEventMessage event) {
        String entry = event.occurredAt() + " | " + event.type() + " | loanId=" + event.loanId()
                + " | member=" + event.memberContact() + " | details=" + event.details();
        auditEntries.add(entry);
    }

    public List<String> entries() {
        synchronized (auditEntries) {
            return List.copyOf(auditEntries);
        }
    }
}
