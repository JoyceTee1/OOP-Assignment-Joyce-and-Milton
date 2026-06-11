package smartlib.domain;

import smartlib.concurrent.ConcurrentInventory;
import smartlib.concurrent.DefaultEventBus;
import smartlib.concurrent.DefaultFineService;
import smartlib.concurrent.DefaultLoanService;
import smartlib.concurrent.DomainNotificationService;
import smartlib.concurrent.InventoryServiceAdapter;
import smartlib.concurrent.ReturnProcessor;
import smartlib.functional.LibraryAnalytics;
import smartlib.functional.LoanSummary;
import smartlib.functional.LoanSummaryCollector;
import smartlib.functional.MethodReferenceAndFunctionalDemo;
import smartlib.generics.Catalogue;
import smartlib.generics.GenericTypeUtils;
import smartlib.generics.TypeTokenRepositoryDemo;
import smartlib.modern.ConsoleMemberNotificationService;
import smartlib.modern.DomainBorrowEventPublisher;
import smartlib.modern.InMemoryReservationService;
import smartlib.modern.LoanResult;
import smartlib.modern.LoanResultHandler;
import smartlib.modern.ModernLibraryFormatter;
import smartlib.patterns.AuditLogListener;
import smartlib.patterns.DecoratorCompositionExample;
import smartlib.patterns.LoanBuilder;
import smartlib.patterns.ReservationBuilder;
import smartlib.patterns.SmartLibPatternComposition;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Beginner-friendly demo: run each assignment area and print what happens.
 * Run: mvn -q exec:java -Dexec.mainClass=smartlib.domain.SmartLibWalkthrough
 */
public final class SmartLibWalkthrough {
    public static void main(String[] args) throws Exception {
        printHeader("STEP 0 — What is this project?");
        System.out.println("""
                SmartLib is a library system written in Java.
                There is NO graphical window — output appears here in the terminal.
                Each step below shows one assignment topic working.
                """);

        step1SolidAndDomain();
        step2Patterns();
        step3Generics();
        step4Functional();
        step5Concurrency();
        step6ModernJava();

        printHeader("DONE — All steps finished");
        System.out.println("Next: run 'mvn test' to verify everything with automated checks.");
    }

    private static void step1SolidAndDomain() {
        printHeader("STEP 1 — SOLID + Domain (return book, fine, report)");
        LibraryService libraryService = SmartLibApplication.createLibraryService();

        System.out.println("Returning overdue loan L-101...");
        double fine = libraryService.returnBook("L-101");
        System.out.println("Fine charged: $" + fine);

        System.out.println("\nCSV report:");
        System.out.println(libraryService.generateReport("CSV"));

        Member member = new Member("M-1", "Alice", "alice@test.com", MembershipType.STANDARD);
        member.setActiveLoansCount(3);
        System.out.println("\nCan Alice borrow another book? " + new StandardPolicy().canBorrow(member));
    }

    private static void step2Patterns() throws Exception {
        printHeader("STEP 2 — Design patterns (Builder, Decorator, Observer)");

        Member member = new Member("M-10", "Bob", "bob@test.com", MembershipType.PREMIUM);
        Book book = Book.simple("B-10", "ISBN-010", "Design Patterns", "Gamma et al.", true);

        Loan builtLoan = new LoanBuilder(member, book).notes("Desk pickup").build();
        System.out.println("Builder created loan " + builtLoan.loanID() + " due " + builtLoan.dueDate());

        Reservation reservation = new ReservationBuilder(member, book).priority(1).build();
        System.out.println("Reservation " + reservation.reservationID() + " expires " + reservation.expiryDate());

        System.out.println("\nDecorator notification (watch log lines):");
        DecoratorCompositionExample.notificationService()
                .send("bob@test.com", "Your book is ready");

        System.out.println("\nObserver event bus (all three listeners wired):");
        LoanManagementService management = new LoanManagementService(
                new InMemoryLoanRepository(List.of(builtLoan)),
                new InMemoryMemberRepository(),
                new InMemoryBookRepository(),
                new StandardFineCalculator()
        );
        AuditLogListener audit = new AuditLogListener();
        SmartLibPatternComposition.eventPublisher(
                SmartLibPatternComposition.wiredEventBus(
                        new smartlib.patterns.DefaultLoanService(management),
                        DecoratorCompositionExample.notificationService(),
                        audit
                )
        ).publishBookReturned(builtLoan.loanID(), "bob@test.com", 0.0);
        System.out.println("Audit log entries: " + audit.entries().size());
    }

    private static void step3Generics() {
        printHeader("STEP 3 — Generics (catalogue search & grouping)");

        Book b1 = new Book("B-1", "ISBN-1", "Clean Code", "Martin", "Software", 2008, 2, 1);
        Book b2 = new Book("B-2", "ISBN-2", "Dune", "Herbert", "Sci-Fi", 1965, 1, 0);

        Catalogue<Book> catalogue = new Catalogue<>();
        catalogue.add(b1);
        catalogue.add(b2);

        System.out.println("Available books: " + catalogue.search(Book::isAvailable).size());
        System.out.println("Books by genre: " + catalogue.groupCount(Book::genre));
        System.out.println("TypeToken args: " + TypeTokenRepositoryDemo.repositoryTypeArguments());
        System.out.println("safeCast works: " + GenericTypeUtils.safeCast(b1, Book.class).map(Book::title).orElse("?"));
    }

    private static void step4Functional() {
        printHeader("STEP 4 — Streams & functional programming");

        Book bookA = Book.simple("B-1", "ISBN-1", "Book A", "Author A", true);
        Book bookB = Book.simple("B-2", "ISBN-2", "Book B", "Author B", false);
        Member member = new Member("M-1", "Sam", "sam@test.com", MembershipType.STUDENT);
        Loan loan = new Loan("L-1", bookA, member, LocalDate.now().minusDays(5), LocalDate.now().plusDays(10),
                null, LoanStatus.ACTIVE, "", 0, null);

        LibraryAnalytics analytics = new LibraryAnalytics(
                List.of(bookA, bookB), List.of(member), List.of(loan), List.of());

        System.out.println("Sorted authors: " + analytics.sortedAuthors());
        System.out.println("Partition available: " + analytics.partitionByAvailability());

        Map<String, Member> members = Map.of(member.id(), member);
        LoanSummary summary = List.of(loan).stream().collect(new LoanSummaryCollector(members, Map.of()));
        System.out.println("Custom collector loan count: " + summary.count());

        System.out.println("Normalized ISBN: "
                + MethodReferenceAndFunctionalDemo.normalizeAndValidateIsbn("978-0-123456-78-9"));
    }

    private static void step5Concurrency() throws Exception {
        printHeader("STEP 5 — Concurrency (inventory + async return)");

        ConcurrentInventory inventory = new ConcurrentInventory(Map.of("ISBN-99", 1));
        System.out.println("Copies before borrow: " + inventory.availableCopies("ISBN-99"));
        boolean borrowed = inventory.borrow("ISBN-99", 100);
        System.out.println("Borrow succeeded? " + borrowed + " | copies left: " + inventory.availableCopies("ISBN-99"));
        inventory.returnCopy("ISBN-99");
        System.out.println("After return: " + inventory.availableCopies("ISBN-99"));

        Loan loan = new Loan("L-900", "B-900", "M-900", "member@test.com",
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(2), LoanStatus.OVERDUE);
        LoanManagementService management = new LoanManagementService(
                new CsvLoanRepository(List.of(loan)),
                new InMemoryMemberRepository(),
                new InMemoryBookRepository(),
                new StandardFineCalculator()
        );

        var pool = Executors.newFixedThreadPool(2);
        try {
            ReturnProcessor processor = new ReturnProcessor(
                    new DefaultLoanService(management),
                    new DefaultFineService(management),
                    new InventoryServiceAdapter(inventory),
                    new DomainNotificationService(new EmailNotificationService()),
                    new DefaultEventBus(SmartLibPatternComposition.wiredEventBus(
                            new smartlib.patterns.DefaultLoanService(management),
                            smartlib.patterns.DecoratorCompositionExample.notificationService()
                    )),
                    pool
            );
            System.out.println("\nAsync return result: " + processor.processReturn("L-900").get());
        } finally {
            pool.shutdownNow();
        }
    }

    private static void step6ModernJava() {
        printHeader("STEP 6 — Modern Java (sealed results, pattern matching)");

        ModernLibraryFormatter formatter = new ModernLibraryFormatter();
        Book book = Book.simple("B-1", "ISBN-1", "Modern Java", "Author", true);
        System.out.println(formatter.describe(book));

        LoanResultHandler handler = new LoanResultHandler(
                new DomainBorrowEventPublisher(new InMemoryLibraryEventRepository()),
                new InMemoryReservationService(
                        new InMemoryReservationRepository(),
                        new InMemoryMemberRepository(),
                        new InMemoryBookRepository()
                ),
                new ConsoleMemberNotificationService(),
                message -> System.out.println("[HANDLER] " + message)
        );

        handler.handle(new LoanResult.Success("L-1", "M-1", "B-1"));
        handler.handle(new LoanResult.FineExceeded("M-1", "member@test.com", 15.0));
    }

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println(title);
        System.out.println("=".repeat(60));
    }
}
