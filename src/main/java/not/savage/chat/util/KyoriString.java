package not.savage.chat.util;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * *Kyori* Adventure String wrapper.
 * @param text The text to wrap
 */
public record KyoriString(String text) {

    public static KyoriString of(String text) {
        return new KyoriString(text);
    }

    public Component color() {
        return MiniMessage.miniMessage().deserialize(text);
    }

    public Component replaceAndColor(Placeholder... placeholders) {
        String message = text;
        for (Placeholder placeholder : placeholders) {
            message = placeholder.replace(message);
        }
        return MiniMessage.miniMessage().deserialize(message);
    }
}
