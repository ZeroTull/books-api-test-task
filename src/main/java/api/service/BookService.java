package api.service;

import config.ConfigReader;
import core.entities.Book;
import core.enums.HttpRequestType;
import core.enums.HttpStatusCode;
import core.restModel.HttpClient;
import core.restModel.RestResponse;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Service-layer wrapper around the Books REST API that exposes
 * domain-centric methods and hides HTTP plumbing behind {@link HttpClient}.
 * <p>
 * <strong>Construction options</strong>
 * <ul>
 *   <li>{@link #BookService()} – create a default client; base-URL read from
 *       <code>config.properties</code>.</li>
 *   <li>{@link #BookService(HttpClient)} – supply your own client; base-URL
 *       still read from config.</li>
 *   <li>{@link #BookService(HttpClient, String)} – full control.</li>
 * </ul>
 */
@Getter
public class BookService {

    private static final String BASE_PATH = "/books";

    protected final HttpClient httpClient;

    // ---------------------------------------------------------------------
    //  Constructors
    // ---------------------------------------------------------------------

    /**
     * Zero-arg constructor: default client + baseUrl from config.
     */
    public BookService() {
        this(new HttpClient(), ConfigReader.get().getBaseUrl());
    }

    /**
     * Use a caller-provided client; baseUrl from config.
     */
    public BookService(HttpClient client) {
        this(client, ConfigReader.get().getBaseUrl());
    }

    /**
     * Full-control constructor.
     */
    public BookService(HttpClient client, String baseUrl) {
        this.httpClient = client;
        io.restassured.RestAssured.baseURI = baseUrl;
    }

    // ---------------------------------------------------------------------
    //  CRUD operations
    // ---------------------------------------------------------------------

    public RestResponse sendRaw(Book book, HttpRequestType requestType, String path, int expectedStatus) {
        return httpClient.send(path, book, requestType, expectedStatus);
    }

    public RestResponse sendRaw(String uri, Object body, HttpRequestType requestType, String username, String password) {
        return httpClient.send(uri, body, requestType, username, password);
    }

    public RestResponse sendRaw(Book book, HttpRequestType requestType, String path) {
        return httpClient.send(path, book, requestType);
    }

    public Book createBook(Book book) {
        return httpClient.post(BASE_PATH, book, Book.class);
    }

    public RestResponse createBookRaw(Book book) {
        return httpClient.post(BASE_PATH, book);
    }

    public Book getBook(int id) {
        return httpClient.get(BASE_PATH + "/" + id, Book.class);
    }

    public RestResponse getBookRaw(int id) {
        return httpClient.send(BASE_PATH + "/" + id, null, HttpRequestType.GET);
    }

    public Book getBook(int id, HttpStatusCode expectedStatus) {
        return httpClient.get(BASE_PATH + "/" + id, Book.class, expectedStatus);
    }

    public List<Book> getAllBooks() {
        Book[] books = httpClient.get(BASE_PATH, Book[].class);
        return Arrays.asList(books);
    }

    public RestResponse getAllBooksRaw() {
        return httpClient.get(BASE_PATH);
    }

    public Book updateBook(int id, Book book) {
        return httpClient.put(BASE_PATH + "/" + id, book, Book.class);
    }

    public RestResponse updateBookRaw(int id, Book book) {
        return httpClient.send(BASE_PATH + "/" + id, book, HttpRequestType.PUT);
    }

    public void deleteBook(String id) {
        httpClient.delete(BASE_PATH + "/" + id);
    }

    public RestResponse deleteBookRaw(String id) {
        return httpClient.deleteRaw(BASE_PATH + "/" + id);
    }

    /* overloaded low-level helper that lets callers specify a full path */
    public RestResponse sendRaw(Object body,
                                HttpRequestType type,
                                String fullPath,
                                int expectedStatus) {
        return httpClient.send(fullPath, body, type, expectedStatus);
    }
}