package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.generics.GenericTypeUtils;
import smartlib.generics.TypeTokenRepositoryDemo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericTypeUtilsTest {
    @Test
    void safeCast_returnsValueForMatchingType() {
        Book book = Book.simple("B-1", "ISBN-1", "Title", "Author", true);
        Optional<Book> cast = GenericTypeUtils.safeCast(book, Book.class);
        assertTrue(cast.isPresent());
        assertEquals("B-1", cast.get().id());
    }

    @Test
    void safeCast_returnsEmptyForMismatchedType() {
        Optional<Book> cast = GenericTypeUtils.safeCast("not-a-book", Book.class);
        assertTrue(cast.isEmpty());
    }

    @Test
    void typeToken_capturesRepositoryGenericArguments() {
        assertEquals(
                java.util.List.of("smartlib.domain.Book", "java.lang.String"),
                TypeTokenRepositoryDemo.repositoryTypeArguments()
        );
    }
}
