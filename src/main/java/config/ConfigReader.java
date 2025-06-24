package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Centralised configuration loader.
 *
 * Lookup order for every key (k = <code>api.baseUrl</code>, <code>api.username</code> …)
 * <ol>
 *   <li> ⚙️ JVM system property   <code>-Dapi.baseUrl=https://…​</code> </li>
 *   <li> 🌱 Environment variable  <code>API_BASEURL=…​</code>        </li>
 *   <li> 📄 config.properties    (in <code>src/test/resources</code>)</li>
 * </ol>
 * …so CI/CD can inject secrets without touching the repo, while local
 * devs can still rely on the file.
 */
public final class ConfigReader {

    private static final String PROP_FILE = "config.properties";
    private static final Properties FILE_PROPS = new Properties();

    /* ------------ eager-load the properties file once ---------------- */
    static {
        try (InputStream in = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream(PROP_FILE)) {
            if (in != null) FILE_PROPS.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read " + PROP_FILE, e);
        }
    }

    /* ------------ singleton facade ---------------------------------- */
    private static final ConfigReader configReader = new ConfigReader();
    public  static ConfigReader get() { return configReader; }
    private ConfigReader() { /* no-op */ }

    /* ---------------------------------------------------------------- */
    /*  strongly-typed helpers used by tests / HttpClient               */
    /* ---------------------------------------------------------------- */

    public String getBaseUrl()  { return valueOf("api.baseUrl",  true);  }
    public String getUsername() { return valueOf("api.username", true);  }
    public String getPassword() { return valueOf("api.password", true);  }

    /** timeout in seconds (defaults to 10 if key absent) */
    public int getTimeout() {
        return Integer.parseInt(Objects.requireNonNull(valueOf("api.timeout", false, "10")));
    }

    /* ---------------------------------------------------------------- */
    /*  generic accessor – public in case you need other keys           */
    /* ---------------------------------------------------------------- */

    public String valueOf(String key, boolean mandatory) {
        return valueOf(key, mandatory, null);
    }

    private String valueOf(String key, boolean mandatory, String fallback) {

        /* 1 ▸ system property (-Dapi.baseUrl=…) */
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;

        /* 2 ▸ environment variable (API_BASEURL=…)   */
        String envKey = key.toUpperCase().replace('.', '_');
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) return env;

        /* 3 ▸ file property (config.properties) */
        String file = FILE_PROPS.getProperty(key);
        if (file != null && !file.isBlank()) return file;

        /* default or error */
        if (fallback != null) return fallback;
        if (mandatory)
            throw new IllegalStateException("Missing configuration for key: " + key);
        return null;
    }
}