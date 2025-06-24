package tests;

import api.service.BookService;
import baseTest.BaseSteps;
import core.entities.Book;
import core.entities.BookBuilder;
import core.enums.HttpRequestType;
import core.enums.HttpStatusCode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import utils.JsonUtils;
import utils.schemaValidation.BookSchemaValidator;
import utils.schemaValidation.ReflectionUtils;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step-definitions for the Books API.
 */
@Epic("Books API")
@Feature("CRUD operations")
@Slf4j
public class BookSteps extends BaseSteps {
    private Book payload;
    private Book latestBook;

    // ---------------------------------------------------------------
    //  GIVEN
    // ---------------------------------------------------------------

    @Given("^A book titled \"([^\"]*)\" by author \"([^\"]*)\"$")
    @Step("Prepare payload with title: {0}, author: {1}")
    public void a_book_payload(String title, String author) {
        payload = BookBuilder.random();
        payload.setName(title);
        payload.setAuthor(author);
        log.info("Generated payload → {}", payload);
    }

    @Given("^I have a new book payload$")
    public void newRandomBookPayload() {
        payload = BookBuilder.random();
    }

    @Given("^I have a book payload with 1MB of text$")
    public void bookWithLargePayload() {
        payload = BookBuilder.random();
        byte[] big = new byte[1_048_576];
        Arrays.fill(big, (byte) 'A');
        payload.setCategory(new String(big, StandardCharsets.US_ASCII));
    }

    @Given("^I have an existing book with id (\\d+)$")
    public void existingBookWithId(int id) {
        latestBook = service().getBook(id);   // fetch & cache
    }

    /* payload carries a different id than path */
    @Given("^I have a book payload with id (\\d+)$")
    public void bookPayloadWithId(int id) {
        payload = BookBuilder.random();
        payload.setId(id);
    }

    /* helper for delete-book scenario */
    @Given("^User creates a Book via API$")
    public void userCreatesBookViaApi() {
        payload = BookBuilder.random();
        latestBook = service().createBook(payload);
    }

    @Given("I have a book payload with {string} in {string} field")
    public void iHaveABookPayloadWithStringInPagesField(String value, String fieldName) {
        payload = BookBuilder.random();
        try {
            Field field = Book.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(payload, value);
            log.info("Set field '{}' to value '{}'", fieldName, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    // ---------------------------------------------------------------
    //  WHEN
    // ---------------------------------------------------------------

    @When("^User sends a \"([^\"]*)\" request to \"([^\"]*)\"$")
    @Step("User sends {0} {1}")
    public void userSendsRequest(String requestType, String path) {
        HttpRequestType type = HttpRequestType.fromString(requestType);
        switch (type) {
            case GET:
                lastResp = service().sendRaw(null, type, path);
                break;
            case POST:
            case PUT:
                lastResp = service().sendRaw(payload == null ? BookBuilder.random() : payload, type, path);
                break;
            default:
                throw new IllegalArgumentException("Unsupported requestType: " + requestType);
        }
        log.info("[API] {} {} → {}", requestType, path, lastResp.getStatus());
    }

    @When("User sends request to get all available books")
    @Step("Get all books via GET /books")
    public void userSendsRequestToGetAllAvailableBooks() {
        lastResp = service().getAllBooksRaw();
    }

    @When("^I create the book$")
    @Step("Create book via POST /books")
    public void i_create_the_book() {
        latestBook = service().sendRaw(payload, HttpRequestType.POST, "/books", HttpStatusCode.OK_200.getCode()).getContent(Book.class);
        log.info("Book created with id={} ", latestBook.getId());
    }

    @When("^I create the book again$")
    @Step("Create book via POST /books")
    public void i_create_the_book_again() {
        lastResp = service().sendRaw(payload, HttpRequestType.POST, "/books");
        log.info("Response = {} ", lastResp.getBody());
    }

    @When("^I retrieve that book by ID$")
    @Step("GET /books/{{id}}")
    public void i_retrieve_that_book_by_id() {
        latestBook = service().getBook(latestBook.getId());
        log.info("Fetched book → {}", latestBook);
    }

    @When("^I update the title to \"([^\"]*)\"$")
    @Step("PUT /books/{{id}} change title to {0}")
    public void i_update_the_title(String newTitle) {
        payload.setName(newTitle);
        latestBook = service().updateBook(latestBook.getId(), payload);
        log.info("Updated book title to '{}'", newTitle);
    }

    @When("^I delete created book$")
    @Step("DELETE /books/{{id}}")
    public void i_delete_created_book() {
        service().deleteBook(String.valueOf(latestBook.getId()));
        log.info("Requested deletion for id={}", latestBook.getId());
    }

    @When("User deletes book with id {string}")
    @Step("DELETE /books/{{id}}")
    public void i_delete_created_book(String bookId) {
        lastResp = service().deleteBookRaw(bookId);
        log.info("Requested deletion for id={}", latestBook.getId());
    }

    @When("^I delete created book again$")
    public void i_delete_created_book_again() {
        lastResp = service().sendRaw(
                null,
                HttpRequestType.DELETE,
                "/books/" + latestBook.getId(),
                HttpStatusCode.NOT_FOUND_404.getCode());
    }

    @When("^User sends a \"([^\"]*)\" request to \"([^\"]*)\" using invalid credentials$")
    @Step("{0} {1} with BAD basic-auth")
    public void userSendsRequestWithInvalidCredentials(String requestType, String path) {
        BookService service = service();
        HttpRequestType httpRequestType = HttpRequestType.fromString(requestType);

        switch (httpRequestType) {
            case GET:
            case DELETE:
                lastResp = service.sendRaw(path, null, httpRequestType, "badUser", "wrongPwd");
                break;
            case PUT:
            case POST:
                lastResp = service.sendRaw(path, BookBuilder.random(), httpRequestType, "badUser", "wrongPwd");
        }
        log.info("{} {} with invalid creds → {}", requestType, path, lastResp.getStatus());
    }

    @When("^I update the book with a mismatched payload ID$")
    @Step("PUT /books/{{id}} with different payload.id – expect 400")
    public void updateWithMismatchedId() {
        // clone the original payload and set a wrong ID
        Book mismatched = latestBook.clone();
        mismatched.setId(latestBook.getId() + 999);

        lastResp = service().sendRaw(mismatched, HttpRequestType.PUT, "/books/" + latestBook.getId());

        log.info("Payload-ID mismatch → {}", lastResp.getStatus());
    }

    @When("I update the book with:")
    public void updateBookWith(DataTable table) {
        payload = latestBook.clone();                       // start with server copy
        Map<String, String> updates = table.asMap(String.class, String.class);

        updates.forEach((k, v) -> ReflectionUtils.setField(Book.class, k, payload, v));
        lastResp = service().updateBookRaw(latestBook.getId(), payload);
    }

    /* generic helper used by Delete-a-book scenario */
    @When("^User sends a \"DELETE\" request to delete created book$")
    public void deleteCreatedBook() {
        lastResp = service().sendRaw(
                null,
                HttpRequestType.DELETE,
                "/books/" + latestBook.getId(),
                HttpStatusCode.OK_200.getCode());
    }

    // ---------------------------------------------------------------
    //  THEN
    // ---------------------------------------------------------------

    @Then("^the created book should have an ID assigned$")
    @Step("Validate server generated an ID")
    public void the_book_should_have_an_id() {
        assertThat(latestBook.getId()).isPositive();
        log.info("Assertion passed: id={} is positive", latestBook.getId());
    }

    @Then("^the title should be \"([^\"]*)\"$")
    @Step("Verify title equals {0}")
    public void the_title_should_be(String expected) {

        assertThat(latestBook.getName()).isEqualTo(expected);
        log.info("Assertion passed: title='{}'", expected);
    }

    @Then("^the author should be \"([^\"]*)\"$")
    @Step("Verify author equals {0}")
    public void the_author_should_be(String expected) {
        assertThat(latestBook.getAuthor()).isEqualTo(expected);
        log.info("Assertion passed: author='{}'", expected);
    }

    @Then("^the response code should be (\\d+)$")
    @Step("Expect status {0}")
    public void responseCodeShouldBe(int expected) {
        assertThat(lastResp.getStatus()).isEqualTo(expected);
    }

    @Then("^I should not be able to fetch that book anymore$")
    @Step("Expect 404 when fetching deleted book")
    public void i_should_not_be_able_to_fetch_that_book() {
        service().getBook(latestBook.getId(), HttpStatusCode.NOT_FOUND_404);
        log.info("Verified deleted book returns 404");
    }

    @And("the response should contain a list of books")
    public void theResponseShouldContainAListOfBooks() {
        Book[] books = JsonUtils.readValue(lastResp.getBody(), Book[].class);
        assertThat(books).isNotEmpty();
    }

    @And("the book should have id {int}")
    public void theBookShouldHaveId(int bookId) {
        assertThat(JsonUtils.readValue(lastResp.getBody(), Book.class).getId()).isEqualTo(bookId);
    }

    @When("I send a request with {string} body to {string}")
    @Step("POST {0} with {body} body")
    public void sendEmptyBody(Object body, String path) {
        lastResp = service().getHttpClient().postRaw(path, body);
        log.info("POST {} with empty body → {}", path, lastResp.getStatus());
    }

    @When("I update the book concurrently with titles")
    @Step("Concurrent PUTs with multiple titles")
    public void updateBookConcurrently(DataTable table) throws InterruptedException {
        latestBook = JsonUtils.readValue(lastResp.getBody(), Book.class);
        List<String> titles = table.asList(String.class);
        ExecutorService pool = Executors.newFixedThreadPool(titles.size());
        CountDownLatch latch = new CountDownLatch(titles.size());

        for (String t : titles) {
            pool.submit(() -> {
                try {
                    Book b = payload.clone();
                    b.setName(t);
                    service().updateBook(latestBook.getId(), b);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();               // wait for both PUTs to finish
        pool.shutdownNow();

        latestBook = service().getBook(latestBook.getId());   // fetch final state
        log.info("Final title after race → {}", latestBook.getName());
    }

    @Then("^the final title should be \"([^\"]*)\"$")
    @Step("Verify final persisted title is {0}")
    public void finalTitleShouldBe(String expected) {
        assertThat(latestBook.getName()).isEqualTo(expected);
    }

    @Then("^the created book data should match the payload$")
    public void createdDataMatches() {
        Book created = JsonUtils.readValue(lastResp.getBody(), Book.class);
        assertThat(created).usingRecursiveComparison()
                .ignoringFields("id")       // id generated by server
                .isEqualTo(payload);
    }

    @Then("^the book should be updated correctly$")
    public void bookUpdatedCorrectly() {
        Book server = JsonUtils.readValue(lastResp.getBody(), Book.class);
        assertThat(server).usingRecursiveComparison()
                .ignoringFields("id")        // id unchanged
                .isEqualTo(payload);
    }

    @Then("^the book should no longer be accessible$")
    public void bookNotAccessible() {
        service().getBook(latestBook.getId(), HttpStatusCode.NOT_FOUND_404);
    }

    @Then("^the JSON response matches the book schema$")
    @Step("Validate JSON matches book schema")
    public void responseMatchesSchema() {
        BookSchemaValidator.assertValid(lastResp.getBody());
    }

    @Given("I have a book payload with all fields blank")
    public void blankPayload() {
        payload = new Book();
    }
}