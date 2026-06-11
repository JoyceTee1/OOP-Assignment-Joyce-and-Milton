package smartlib.generics;

public interface Borrowable {
    String getId();
    String getTitle();
    boolean isAvailable();
    int getAvailableCopies();
}
