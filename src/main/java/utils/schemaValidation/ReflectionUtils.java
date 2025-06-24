package utils.schemaValidation;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

/**
 * Minimal helper to set private fields via reflection.
 * **Not** meant for production – only test payload tweaking.
 */
public final class ReflectionUtils {

    private ReflectionUtils() {
    }      // no‐instance

    @SneakyThrows
    public static void setField(Class<?> clazz,
                                String fieldName,
                                Object target,
                                Object rawValue) {

        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);

        /* ---------- simple coercion ---------- */
        Object value = rawValue;                   // default: unchanged

        Class<?> t = f.getType();
        if (rawValue instanceof String s) {
            if (t == int.class || t == Integer.class) value = Integer.parseInt(s);
            else if (t == long.class || t == Long.class) value = Long.parseLong(s);
            else if (t == double.class || t == Double.class) value = Double.parseDouble(s);
            else if (t == float.class || t == Float.class) value = Float.parseFloat(s);
            else if (t == boolean.class || t == Boolean.class) value = Boolean.parseBoolean(s);
            else if (t.isEnum()) value = Enum.valueOf((Class) t, s);
        }
        f.set(target, value);
    }
}
