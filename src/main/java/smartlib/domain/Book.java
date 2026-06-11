package smartlib.domain;

import smartlib.generics.Borrowable;

public record Book(
        String id,
        String isbn,
        String title,
        String author,
        String genre,
        int yearPublished,
        int totalCopies,
        int availableCopies
) implements Borrowable, Comparable<Book> {
    public Book {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("isbn must not be blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("author must not be blank");
        }
        if (genre == null || genre.isBlank()) {
            throw new IllegalArgumentException("genre must not be blank");
        }
        if (yearPublished < 0) {
            throw new IllegalArgumentException("yearPublished must be >= 0");
        }
        if (totalCopies < 0) {
            throw new IllegalArgumentException("totalCopies must be >= 0");
        }
        if (availableCopies < 0 || availableCopies > totalCopies) {
            throw new IllegalArgumentException("availableCopies must be between 0 and totalCopies");
        }
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getTitle() { return title; }

    @Override
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    @Override
    public int getAvailableCopies() { return availableCopies; }

    @Override
    public int compareTo(Book other) {
        return title.compareToIgnoreCase(other.title);
    }

    /** Compact constructor for tests and demos that only need availability flag. */
    public static Book simple(String id, String isbn, String title, String author, boolean available) {
        int copies = available ? 1 : 0;
        return new Book(id, isbn, title, author, "General", 2020, copies, copies);
    }
}
