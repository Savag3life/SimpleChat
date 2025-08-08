package not.savage.chat.util;

import org.jetbrains.annotations.NotNull;

/**
 * Simple "Placeholder" structure for use in messages.
 * Automatically adds % and % around the tag.
 * @param tag the placeholder tag, e.g. "player", "amount"
 * @param value the value to replace the placeholder with, e.g. "Steve", "1000"
 */
public record Placeholder(String tag, String value) {

    public static Placeholder of(String tag, Object value) {
        return new Placeholder(tag, value.toString());
    }

    @Override
    public @NotNull String toString() {
        return "%" + tag + "%";
    }

    public String replace(String message) {
        return message.replace(toString(), value);
    }
}
