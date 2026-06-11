package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.generics.Borrowable;
import smartlib.generics.Catalogue;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogueTest {
    @Test
    void groupCount_producesGenreFrequencyAndYearHistogram() {
        Catalogue<DemoBook> catalogue = new Catalogue<>();
        catalogue.add(new DemoBook("Clean Code", "Software", 2008, true));
        catalogue.add(new DemoBook("Effective Java", "Software", 2018, true));
        catalogue.add(new DemoBook("Dune", "Sci-Fi", 1965, true));
        catalogue.add(new DemoBook("Children of Dune", "Sci-Fi", 1976, false));
        catalogue.add(new DemoBook("The Pragmatic Programmer", "Software", 1999, true));

        Map<String, Long> genreFrequency = catalogue.groupCount(DemoBook::genre);
        Map<Integer, Long> publicationYearHistogram = catalogue.groupCount(DemoBook::publicationYear);

        assertEquals(3L, genreFrequency.get("Software"));
        assertEquals(2L, genreFrequency.get("Sci-Fi"));

        assertEquals(1L, publicationYearHistogram.get(2008));
        assertEquals(1L, publicationYearHistogram.get(2018));
        assertEquals(1L, publicationYearHistogram.get(1965));
        assertEquals(1L, publicationYearHistogram.get(1976));
        assertEquals(1L, publicationYearHistogram.get(1999));
    }

    @Test
    void addAllAvailable_copiesOnlyAvailableItems() {
        Catalogue<DemoBook> source = new Catalogue<>();
        source.add(new DemoBook("Book A", "Fiction", 2020, true));
        source.add(new DemoBook("Book B", "Fiction", 2021, false));

        Catalogue<DemoBook> target = new Catalogue<>();
        target.addAllAvailable(source);

        List<DemoBook> copied = target.items();
        assertEquals(1, copied.size());
        assertEquals("Book A", copied.get(0).title());
    }

    @Test
    void updateMatching_appliesConsumerToMatchingItems() {
        Catalogue<MutableDemoBook> catalogue = new Catalogue<>();
        MutableDemoBook available = new MutableDemoBook("A", "Fiction", 2020, true);
        MutableDemoBook unavailable = new MutableDemoBook("B", "Fiction", 2021, false);
        catalogue.add(available);
        catalogue.add(unavailable);

        catalogue.updateMatching(MutableDemoBook::isAvailable, book -> book.setFeatured(true));

        assertEquals(true, available.isFeatured());
        assertEquals(false, unavailable.isFeatured());
    }

    private record DemoBook(String title, String genre, int publicationYear, boolean available)
            implements Borrowable, Comparable<DemoBook> {
        @Override public String getId() { return title; }
        @Override public String getTitle() { return title; }
        @Override public boolean isAvailable() { return available; }
        @Override public int getAvailableCopies() { return available ? 1 : 0; }

        @Override
        public int compareTo(DemoBook other) {
            return title.compareTo(other.title);
        }
    }

    private static final class MutableDemoBook implements Borrowable, Comparable<MutableDemoBook> {
        private final String title;
        private final String genre;
        private final int publicationYear;
        private final boolean available;
        private boolean featured;

        private MutableDemoBook(String title, String genre, int publicationYear, boolean available) {
            this.title = title;
            this.genre = genre;
            this.publicationYear = publicationYear;
            this.available = available;
        }

        @Override public String getId() { return title; }
        @Override public String getTitle() { return title; }
        @Override public boolean isAvailable() { return available; }
        @Override public int getAvailableCopies() { return available ? 1 : 0; }

        public boolean isFeatured() {
            return featured;
        }

        public void setFeatured(boolean featured) {
            this.featured = featured;
        }

        @Override
        public int compareTo(MutableDemoBook other) {
            return title.compareTo(other.title);
        }
    }
}
