package smartlib.functional;

import smartlib.domain.Book;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public final class MethodReferenceAndFunctionalDemo {
    private MethodReferenceAndFunctionalDemo() {
    }

    public static Optional<Book> findByIsbn(List<Book> books, String isbn) {
        return books.stream()
                .filter(book -> book.isbn().equals(isbn))
                .findFirst();
    }

    public static MethodReferenceExamples methodReferenceExamples(List<Book> books) {
        BiFunction<List<Book>, String, Optional<Book>> staticMethodRef = MethodReferenceAndFunctionalDemo::findByIsbn;
        Function<Book, String> unboundInstanceMethodRef = Book::title;

        List<Book> specificCatalogue = books.stream().limit(2).toList();
        Predicate<Book> boundInstanceMethodRef = specificCatalogue::contains;

        MemberFactory constructorRef = Member::new;
        Member createdMember = constructorRef.create("M-100", "Alice", "alice@smartlib.test", MembershipType.STUDENT);

        Optional<Book> foundByStaticRef = staticMethodRef.apply(books, "ISBN-001");
        String firstTitle = books.isEmpty() ? "" : unboundInstanceMethodRef.apply(books.get(0));
        boolean firstInSpecificCatalogue = !books.isEmpty() && boundInstanceMethodRef.test(books.get(0));

        return new MethodReferenceExamples(
                foundByStaticRef,
                firstTitle,
                firstInSpecificCatalogue,
                createdMember
        );
    }

    public static PredicateDemoResult predicateCompositionDemo(List<Book> books, Map<String, Integer> publicationYearByBookId) {
        Predicate<Book> available = Book::isAvailable;
        Predicate<Book> publishedAfter2010 = book -> publicationYearByBookId.getOrDefault(book.id(), 0) > 2010;

        Predicate<Book> availableAndPublishedAfter2010 = available.and(publishedAfter2010);
        Predicate<Book> availableOrPublishedAfter2010 = available.or(publishedAfter2010);
        Predicate<Book> negatedAvailableAndPublishedAfter2010 = availableAndPublishedAfter2010.negate();

        long andCount = books.stream().filter(availableAndPublishedAfter2010).count();
        long orCount = books.stream().filter(availableOrPublishedAfter2010).count();
        long negatedCount = books.stream().filter(negatedAvailableAndPublishedAfter2010).count();

        return new PredicateDemoResult(andCount, orCount, negatedCount);
    }

    public static Function<String, String> isbnNormalizationAndThenPipeline() {
        Function<String, String> stripHyphens = isbn -> isbn.replace("-", "");
        Function<String, String> toUpperCase = String::toUpperCase;
        Function<String, String> validateLength = isbn -> {
            if (isbn.length() == 10 || isbn.length() == 13) {
                return isbn;
            }
            throw new IllegalArgumentException("ISBN length must be 10 or 13 after normalization");
        };
        return stripHyphens.andThen(toUpperCase).andThen(validateLength);
    }

    public static Function<String, String> isbnNormalizationComposePipeline() {
        Function<String, String> stripHyphens = isbn -> isbn.replace("-", "");
        Function<String, String> toUpperCase = String::toUpperCase;
        Function<String, String> validateLength = isbn -> {
            if (isbn.length() == 10 || isbn.length() == 13) {
                return isbn;
            }
            throw new IllegalArgumentException("ISBN length must be 10 or 13 after normalization");
        };
        return validateLength.compose(toUpperCase).compose(stripHyphens);
    }

    public static String normalizeAndValidateIsbn(String rawIsbn) {
        return isbnNormalizationAndThenPipeline().apply(rawIsbn);
    }

    @FunctionalInterface
    public interface MemberFactory {
        Member create(String id, String name, String email, MembershipType membershipType);
    }

    public record MethodReferenceExamples(
            Optional<Book> staticMethodResult,
            String unboundInstanceResult,
            boolean boundInstanceResult,
            Member constructorResult
    ) {
    }

    public record PredicateDemoResult(long andCount, long orCount, long negatedCount) {
    }
}
