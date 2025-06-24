package tests;

import api.service.BookService;
import io.cucumber.java.Before;

/**
 * Owns all hooks.  Must have a public zero-arg ctor so Cucumber can instantiate
 * it via reflection.
 */
public class Hooks {
    private static final ThreadLocal<BookService> SERVICE = new ThreadLocal<>();

    public Hooks() { /* public zero-arg ctor required */ }

    @Before(order = 0)
    public void initService() {
        SERVICE.set(new BookService());          // baseUrl & auth from config
    }

    /* package-private getter used by BaseSteps */
    public static BookService getService() {
        return SERVICE.get();
    }
}