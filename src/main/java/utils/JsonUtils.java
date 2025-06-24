package utils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.IOException;

/**
 * Tiny Jackson wrapper used across the test-suite for quick JSON ↔ POJO mapping.
 * <p>
 * We purposefully keep the surface minimal – just a couple of static helpers – so
 * we don’t leak Jackson all over the step-definitions.  If we ever want to
 * migrate to another library (e.g. Gson), we only need to change this one file.
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonUtils() { /* static - helpers only */ }

    // ---------------------------------------------------------------------
    //  Generic JSON → Object (the method BookService & HttpClient rely on)
    // ---------------------------------------------------------------------

    /**
     * Deserialise JSON string into the given target class.
     *
     * @throws RuntimeException if parsing fails – bubbles up to the test so it
     *                          surfaces as a failure.
     */
    public static <T> T readValue(String json, Class<T> target) {
        try {
            return MAPPER.readValue(json, target);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON into " + target.getSimpleName(), e);
        }

    }

    @SneakyThrows
    public static <T> T read(String input, Class<T> clazz, Module... modules) {
        return _read(input, clazz, modules);
    }

    private static synchronized <T> T _read(String input, Class<T> clazz, Module... modules) {
        try {
            ObjectMapper mapper = getObjectMapper(modules);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(input, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Could not read json from " + input, e);
        }
    }

    /**
     * Overload for complex generics (List<Book>, Map<String,Book> …).
     */
    public static <T> T readValue(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    // ---------------------------------------------------------------------
    //  Object → JSON ( occasionally handy in step-defs )
    // ---------------------------------------------------------------------

    public static String writeValue(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialise object to JSON", e);
        }
    }

    private static synchronized ObjectMapper getObjectMapper(Module... modules) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.registerModules(modules);
        return mapper;
    }
}