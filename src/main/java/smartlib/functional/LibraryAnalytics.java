package smartlib.functional;

import smartlib.domain.Book;
import smartlib.domain.Fine;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class LibraryAnalytics {
    private final List<Book> books;
    private final List<Member> members;
    private final List<Loan> loans;
    private final List<Fine> fines;
    private final Map<String, Book> booksById;
    private final Map<String, Member> membersById;
    private final Map<String, Loan> loansById;
    private final Clock clock;

    public LibraryAnalytics(List<Book> books, List<Member> members, List<Loan> loans, List<Fine> fines) {
        this(books, members, loans, fines, Clock.systemDefaultZone());
    }

    public LibraryAnalytics(
            List<Book> books,
            List<Member> members,
            List<Loan> loans,
            List<Fine> fines,
            Clock clock
    ) {
        this.books = new ArrayList<>(Objects.requireNonNull(books, "books must not be null"));
        this.members = new ArrayList<>(Objects.requireNonNull(members, "members must not be null"));
        this.loans = new ArrayList<>(Objects.requireNonNull(loans, "loans must not be null"));
        this.fines = new ArrayList<>(Objects.requireNonNull(fines, "fines must not be null"));
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.booksById = this.books.stream()
                .collect(Collectors.toUnmodifiableMap(Book::id, Function.identity()));
        this.membersById = this.members.stream()
                .collect(Collectors.toUnmodifiableMap(Member::id, Function.identity()));
        this.loansById = this.loans.stream()
                .collect(Collectors.toUnmodifiableMap(Loan::id, Function.identity()));
    }

    public List<Book> topBorrowedBooks(int n) {
        if (n <= 0) {
            return List.of();
        }

        return loans.stream()
                .map(loan -> booksById.get(loan.bookId()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<Member> overdueMembers() {
        LocalDate today = LocalDate.now(clock);
        return loans.stream()
                .filter(loan -> loan.returnedOn() == null)
                .filter(loan -> loan.dueDate().isBefore(today))
                .map(loan -> membersById.get(loan.memberId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public Map<MembershipType, Double> avgFineByMembership() {
        return fines.stream()
                .map(this::membershipFineEntry)
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.averagingDouble(Map.Entry::getValue)
                ));
    }

    public List<String> sortedAuthors() {
        return books.stream()
                .map(Book::author)
                .distinct()
                .sorted()
                .toList();
    }

    public Map<Boolean, List<Book>> partitionByAvailability() {
        return books.stream()
                .collect(Collectors.partitioningBy(Book::isAvailable));
    }

    public Optional<String> isbnListForAuthor(String author) {
        if (author == null || author.isBlank()) {
            return Optional.empty();
        }

        String joined = books.stream()
                .filter(book -> book.author().equalsIgnoreCase(author))
                .map(Book::isbn)
                .collect(Collectors.joining(", "));

        return joined.isBlank() ? Optional.empty() : Optional.of(joined);
    }

    public Map<Integer, Long> loansPerMonth(int year) {
        return loans.stream()
                .filter(loan -> loan.borrowedOn().getYear() == year)
                .collect(Collectors.groupingBy(
                        loan -> loan.borrowedOn().getMonthValue(),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    private Optional<Map.Entry<MembershipType, Double>> membershipFineEntry(Fine fine) {
        return Optional.ofNullable(loansById.get(fine.loanId()))
                .map(Loan::memberId)
                .map(membersById::get)
                .map(member -> Map.entry(member.membershipType(), fine.amount()));
    }
}
