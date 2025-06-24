package core.entities;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Helper for generating ready-to-post {@link Book} payloads filled with random—but
 * <em>plausible</em>—data.  Useful for data-driven tests or when you need
 * unique books for parallel execution.
 *
 * <pre>
 * Book random = BookBuilder.random();
 * </pre>
 */
public final class BookBuilder {

    private static final String[] TITLES = {
            "The Pragmatic Programmer", "Clean Code", "Refactoring",
            "Domain-Driven Design", "Effective Java", "Design Patterns",
            "Test-Driven Development", "Continuous Delivery"
    };

    private static final String[] AUTHORS = {
            "Kent Beck", "Martin Fowler", "Robert C. Martin",
            "Erich Gamma", "Brian Goetz", "Eric Evans",
            "Andrew Hunt", "Dave Thomas"
    };

    private static final String[] PUBLISHERS = {
            "Addison-Wesley", "O'Reilly", "Prentice Hall", "Manning",
            "Pearson", "No Starch Press"
    };

    private static final String[] CATEGORIES = {
            "Software", "Architecture", "Agile", "DevOps",
            "Databases", "Cloud", "Security"
    };

    public BookBuilder() {}

    /** Return a fully-populated {@link Book} with random data. */
    public static Book random() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        Book b = new Book();
        b.setId(null);   // server side generated
        b.setName(randomOf(TITLES, rnd));
        b.setAuthor(randomOf(AUTHORS, rnd));
        b.setPublication(randomOf(PUBLISHERS, rnd) + " " + (1990 + rnd.nextInt(35)));
        b.setCategory(randomOf(CATEGORIES, rnd));
        b.setPages(String.valueOf(10 + rnd.nextInt(900)));  // 10–999 pages
        b.setPrice(String.valueOf(Math.round((5 + rnd.nextDouble(95)) * 100) / 100.0)); // 5.00–99.99
        return b;
    }

    private static String randomOf(String[] arr, ThreadLocalRandom rnd) {
        return arr[rnd.nextInt(arr.length)];
    }
}