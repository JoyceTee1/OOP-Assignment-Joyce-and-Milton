package smartlib;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.Fine;
import smartlib.domain.Loan;
import smartlib.domain.LoanStatus;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.functional.LibraryAnalytics;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryAnalyticsTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-15T12:00:00Z"),
            ZoneOffset.UTC
    );

    private Book bookA;
    private Book bookB;
    private Member standardMember;
    private Member premiumMember;
    private LibraryAnalytics analytics;

    @BeforeEach
    void setUp() {
        bookA = new Book("B-1", "ISBN-1", "Clean Code", "Robert Martin", "Software", 2008, 2, 1);
        bookB = new Book("B-2", "ISBN-2", "Effective Java", "Joshua Bloch", "Software", 2018, 1, 0);
        standardMember = new Member("M-1", "Alice", "alice@test.com", MembershipType.STANDARD);
        premiumMember = new Member("M-2", "Bob", "bob@test.com", MembershipType.PREMIUM);

        Loan loan1 = new Loan(
                "L-1", bookA, standardMember,
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 24),
                null, LoanStatus.ACTIVE, "", 0, null
        );
        Loan loan2 = new Loan(
                "L-2", bookA, premiumMember,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 15),
                null, LoanStatus.ACTIVE, "", 0, null
        );
        Loan overdueLoan = new Loan(
                "L-3", bookB, standardMember,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10),
                null, LoanStatus.OVERDUE, "", 0, null
        );

        Fine standardFine = Fine.fromOverdueLoan(overdueLoan, 4);
        overdueLoan.attachFine(standardFine);

        Fine premiumFine = new Fine(
                "F-2",
                loan2,
                2,
                6.0,
                "Overdue return",
                null
        );

        analytics = new LibraryAnalytics(
                List.of(bookA, bookB),
                List.of(standardMember, premiumMember),
                List.of(loan1, loan2, overdueLoan),
                List.of(standardFine, premiumFine),
                FIXED_CLOCK
        );
    }

    @Test
    void topBorrowedBooks_returnsHighestFrequencyFirst() {
        List<Book> top = analytics.topBorrowedBooks(1);

        assertEquals(1, top.size());
        assertEquals("B-1", top.get(0).id());
    }

    @Test
    void topBorrowedBooks_returnsEmptyWhenNIsZero() {
        assertTrue(analytics.topBorrowedBooks(0).isEmpty());
        assertTrue(analytics.topBorrowedBooks(-1).isEmpty());
    }

    @Test
    void overdueMembers_returnsDistinctMembersWithActiveOverdueLoans() {
        List<Member> overdue = analytics.overdueMembers();

        assertEquals(2, overdue.size());
        assertTrue(overdue.stream().anyMatch(member -> member.id().equals("M-1")));
        assertTrue(overdue.stream().anyMatch(member -> member.id().equals("M-2")));
    }

    @Test
    void overdueMembers_respectsInjectedClock() {
        Loan notYetDue = new Loan(
                "L-99", bookA, standardMember,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1),
                null, LoanStatus.ACTIVE, "", 0, null
        );
        LibraryAnalytics earlyAnalytics = new LibraryAnalytics(
                List.of(bookA),
                List.of(standardMember),
                List.of(notYetDue),
                List.of(),
                FIXED_CLOCK
        );

        assertTrue(earlyAnalytics.overdueMembers().isEmpty());
    }

    @Test
    void avgFineByMembership_averagesFinesPerMembershipType() {
        Map<MembershipType, Double> averages = analytics.avgFineByMembership();

        assertEquals(2.0, averages.get(MembershipType.STANDARD), 0.001);
        assertEquals(6.0, averages.get(MembershipType.PREMIUM), 0.001);
    }

    @Test
    void sortedAuthors_returnsDistinctAuthorsAlphabetically() {
        assertEquals(List.of("Joshua Bloch", "Robert Martin"), analytics.sortedAuthors());
    }

    @Test
    void partitionByAvailability_splitsBooksByAvailability() {
        Map<Boolean, List<Book>> partitions = analytics.partitionByAvailability();

        assertEquals(1, partitions.get(true).size());
        assertEquals(1, partitions.get(false).size());
        assertEquals("B-1", partitions.get(true).get(0).id());
        assertEquals("B-2", partitions.get(false).get(0).id());
    }

    @Test
    void isbnListForAuthor_returnsJoinedIsbnsWhenAuthorMatches() {
        Optional<String> isbns = analytics.isbnListForAuthor("robert martin");

        assertTrue(isbns.isPresent());
        assertEquals("ISBN-1", isbns.get());
    }

    @Test
    void isbnListForAuthor_returnsEmptyWhenAuthorUnknownOrBlank() {
        assertTrue(analytics.isbnListForAuthor("Unknown Author").isEmpty());
        assertTrue(analytics.isbnListForAuthor("  ").isEmpty());
        assertTrue(analytics.isbnListForAuthor(null).isEmpty());
    }

    @Test
    void loansPerMonth_countsLoansGroupedByMonthInSortedMap() {
        Map<Integer, Long> perMonth = analytics.loansPerMonth(2026);

        assertEquals(2L, perMonth.get(1));
        assertEquals(1L, perMonth.get(2));
        assertEquals(List.of(1, 2), List.copyOf(perMonth.keySet()));
    }
}
