package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.patterns.LoanBuilder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanBuilderTest {
    @Test
    void minimalLoan_onlyMemberAndBook_usesDefaults() {
        Member member = new Member("M-1", "Alice", "alice@test.com", MembershipType.STANDARD);
        Book book = Book.simple("B-1", "ISBN-001", "Clean Code", "Robert Martin", true);

        Loan loan = new LoanBuilder(member, book).build();

        assertNotNull(loan.loanID());
        assertEquals(LocalDate.now(), loan.loanDate());
        assertEquals(LocalDate.now().plusDays(14), loan.dueDate());
        assertEquals("", loan.notes());
        assertEquals(0, loan.renewalCount());
        assertNull(loan.referenceCode());
    }

    @Test
    void fullLoan_allOptionalFieldsPreserved() {
        Member member = new Member("M-2", "Bob", "bob@test.com", MembershipType.PREMIUM);
        Book book = Book.simple("B-2", "ISBN-002", "Domain-Driven Design", "Eric Evans", true);

        Loan loan = new LoanBuilder(member, book)
                .notes("Handle with care")
                .renewalCount(2)
                .referenceCode("REF-900")
                .build();

        assertNotNull(loan.loanID());
        assertEquals(LocalDate.now(), loan.loanDate());
        assertEquals(LocalDate.now().plusDays(28), loan.dueDate());
        assertEquals("Handle with care", loan.notes());
        assertEquals(2, loan.renewalCount());
        assertEquals("REF-900", loan.referenceCode());
    }

    @Test
    void negativeRenewal_throwsIllegalArgumentException() {
        Member member = new Member("M-3", "Sara", "sara@test.com", MembershipType.STUDENT);
        Book book = Book.simple("B-3", "ISBN-003", "Refactoring", "Martin Fowler", true);

        LoanBuilder builder = new LoanBuilder(member, book).renewalCount(-1);

        assertThrows(IllegalArgumentException.class, builder::build);
    }
}
