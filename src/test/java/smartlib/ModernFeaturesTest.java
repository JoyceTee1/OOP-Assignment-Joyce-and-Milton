package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.BorrowingPolicy;
import smartlib.domain.InMemoryLibraryEventRepository;
import smartlib.domain.InMemoryReservationRepository;
import smartlib.domain.Loan;
import smartlib.domain.LoanStatus;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.Notification;
import smartlib.domain.NotificationChannel;
import smartlib.domain.PremiumPolicy;
import smartlib.domain.StandardPolicy;
import smartlib.domain.StudentPolicy;
import smartlib.modern.ConsoleMemberNotificationService;
import smartlib.modern.DomainBorrowEventPublisher;
import smartlib.modern.InMemoryReservationService;
import smartlib.modern.LoanResult;
import smartlib.modern.LoanResultHandler;
import smartlib.modern.ModernLibraryFormatter;
import smartlib.domain.InMemoryBookRepository;
import smartlib.domain.InMemoryMemberRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModernFeaturesTest {
    @Test
    void loanResultHandler_handlesAllSealedVariants() {
        List<String> logs = new ArrayList<>();
        InMemoryMemberRepository memberRepository = new InMemoryMemberRepository();
        InMemoryBookRepository bookRepository = new InMemoryBookRepository();
        Member member = new Member("M-1", "Alice", "alice@test.com", MembershipType.STANDARD);
        Book book = Book.simple("B-1", "ISBN-1", "Clean Code", "Robert Martin", false);
        memberRepository.save(member);
        bookRepository.save(book);

        LoanResultHandler handler = new LoanResultHandler(
                new DomainBorrowEventPublisher(new InMemoryLibraryEventRepository()),
                new InMemoryReservationService(new InMemoryReservationRepository(), memberRepository, bookRepository),
                new ConsoleMemberNotificationService(),
                logs::add
        );

        handler.handle(new LoanResult.Success("L-1", "M-1", "B-1"));
        handler.handle(new LoanResult.InsufficientCopies("M-1", "alice@test.com", "B-1"));
        handler.handle(new LoanResult.MemberSuspended("M-1", "Unpaid fines"));
        handler.handle(new LoanResult.FineExceeded("M-1", "alice@test.com", 12.5));

        assertTrue(logs.stream().anyMatch(line -> line.contains("Loan created")));
        assertTrue(logs.stream().anyMatch(line -> line.contains("suspended")));
    }

    @Test
    void modernLibraryFormatter_describesAllEntityTypes() {
        ModernLibraryFormatter formatter = new ModernLibraryFormatter();

        // Book
        Book book = Book.simple("B-1", "ISBN-1", "Clean Code", "Robert Martin", true);
        assertTrue(formatter.describe(book).contains("Book"));
        assertTrue(formatter.describe(book).contains("Clean Code"));

        // Member
        Member member = new Member("M-1", "Alice", "alice@test.com", MembershipType.PREMIUM);
        assertTrue(formatter.describe(member).contains("Member"));
        assertTrue(formatter.describe(member).contains("Alice"));

        // Loan
        Loan loan = new Loan("L-1", "B-1", "M-1", "alice@test.com",
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(9), LoanStatus.ACTIVE);
        assertTrue(formatter.describe(loan).contains("Loan"));
        assertTrue(formatter.describe(loan).contains("L-1"));

        // Notification
        Notification notification = new Notification("N-1", "M-1", NotificationChannel.EMAIL,
                "Your book is due", Instant.now());
        assertTrue(formatter.describe(notification).contains("Notification"));
        assertTrue(formatter.describe(notification).contains("N-1"));

        // Unknown type falls back to class name
        assertTrue(formatter.describe("not a domain object").contains("Unknown"));
    }

    @Test
    void policyFor_switchExpression_returnsCorrectPolicyForEachMembershipType() {
        ModernLibraryFormatter formatter = new ModernLibraryFormatter();

        BorrowingPolicy standard = formatter.policyFor(MembershipType.STANDARD);
        BorrowingPolicy premium  = formatter.policyFor(MembershipType.PREMIUM);
        BorrowingPolicy student  = formatter.policyFor(MembershipType.STUDENT);

        assertInstanceOf(StandardPolicy.class, standard);
        assertInstanceOf(PremiumPolicy.class,  premium);
        assertInstanceOf(StudentPolicy.class,  student);

        assertEquals(3,  standard.maxBooksAllowed());
        assertEquals(10, premium.maxBooksAllowed());
        assertEquals(5,  student.maxBooksAllowed());
    }

    @Test
    void buildNotificationPayload_textBlock_containsMemberData() {
        ModernLibraryFormatter formatter = new ModernLibraryFormatter();
        Member member = new Member("M-42", "Bob", "bob@smartlib.test", MembershipType.STUDENT);

        String payload = formatter.buildNotificationPayload(member, "Your loan is overdue");

        assertTrue(payload.contains("M-42"));
        assertTrue(payload.contains("Bob"));
        assertTrue(payload.contains("Your loan is overdue"));
    }
}
