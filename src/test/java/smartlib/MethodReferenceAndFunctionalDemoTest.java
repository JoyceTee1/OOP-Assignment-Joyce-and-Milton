package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.MembershipType;
import smartlib.functional.MethodReferenceAndFunctionalDemo;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodReferenceAndFunctionalDemoTest {
    @Test
    void normalizeAndValidateIsbn_andThenMatchesComposePipeline() {
        String raw = "978-0-123456-78-9";
        String viaAndThen = MethodReferenceAndFunctionalDemo.isbnNormalizationAndThenPipeline().apply(raw);
        String viaCompose = MethodReferenceAndFunctionalDemo.isbnNormalizationComposePipeline().apply(raw);

        assertEquals("9780123456789", viaAndThen);
        assertEquals(viaAndThen, viaCompose);
        assertEquals(viaAndThen, MethodReferenceAndFunctionalDemo.normalizeAndValidateIsbn(raw));
    }

    @Test
    void normalizeAndValidateIsbn_rejectsInvalidLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MethodReferenceAndFunctionalDemo.normalizeAndValidateIsbn("123")
        );
    }

    @Test
    void methodReferenceExamples_coverAllFourReferenceKinds() {
        Book book = Book.simple("B-1", "ISBN-001", "Clean Code", "Robert Martin", true);
        var examples = MethodReferenceAndFunctionalDemo.methodReferenceExamples(List.of(book));

        assertTrue(examples.staticMethodResult().isPresent());
        assertEquals("ISBN-001", examples.staticMethodResult().get().isbn());
        assertEquals("Clean Code", examples.unboundInstanceResult());
        assertTrue(examples.boundInstanceResult());
        assertEquals("M-100", examples.constructorResult().id());
        assertEquals(MembershipType.STUDENT, examples.constructorResult().membershipType());
    }

    @Test
    void predicateCompositionDemo_appliesAndOrNegate() {
        Book availableRecent = Book.simple("B-1", "ISBN-1", "A", "Author", true);
        Book unavailableRecent = Book.simple("B-2", "ISBN-2", "B", "Author", false);
        Book availableOld = Book.simple("B-3", "ISBN-3", "C", "Author", true);

        Map<String, Integer> years = Map.of(
                "B-1", 2018,
                "B-2", 2015,
                "B-3", 2005
        );

        var result = MethodReferenceAndFunctionalDemo.predicateCompositionDemo(
                List.of(availableRecent, unavailableRecent, availableOld),
                years
        );

        assertEquals(1L, result.andCount());
        assertEquals(3L, result.orCount());
        assertEquals(2L, result.negatedCount());
    }
}
