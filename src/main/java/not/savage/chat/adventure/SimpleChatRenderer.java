package not.savage.chat.adventure;

import io.papermc.paper.chat.ChatRenderer;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import not.savage.chat.SimpleChat;
import not.savage.chat.util.KyoriString;
import not.savage.chat.util.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Viewer UnAware Chat Renderer for SimpleChat.
 */
public class SimpleChatRenderer implements ChatRenderer {

    private final SimpleChat plugin;
    private final LuckPerms luckPerms;
    private final Map<String, KyoriString> formats = new HashMap<>();
    private final boolean usePlaceholderAPI;
    private final boolean replaceLegacyAmpersand;

    public SimpleChatRenderer(final SimpleChat plugin) {
        this.plugin = plugin;

        // Setup Luck Perms
        final RegisteredServiceProvider<LuckPerms> lpsp = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
        luckPerms = (lpsp != null) ? lpsp.getProvider() : null;
        if (luckPerms == null) {
            plugin.getLogger().severe("LuckPerms is not available! SimpleChat will not function properly.");
            throw new IllegalStateException("LuckPerms is required for SimpleChat to function.");
        }
        plugin.getLogger().info("LuckPerms found, using it for chat formatting.");

        // Setup PlaceholderAPI
        usePlaceholderAPI = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (usePlaceholderAPI) {
            plugin.getLogger().info("PlaceholderAPI found, placeholders will be processed in message format.");
        } else {
            plugin.getLogger().warning("PlaceholderAPI not found, placeholders will not be processed in message format.");
        }

        replaceLegacyAmpersand = plugin.getConfig().getBoolean("support_legacy_ampersand", true);

        // Make sure the config has at least one format defined
        if (!plugin.getConfig().isConfigurationSection("formats")) {
            plugin.getLogger().warning("No chat formats found in config! Please define at least a 'default' format.");
            throw new IllegalStateException("Chat formats are not defined in the config.");
        }

        plugin.getConfig().getConfigurationSection("formats").getKeys(false).forEach(key -> {
            String formatString = plugin.getConfig().getString("formats." + key);
            formats.put(key, new KyoriString(formatString));
        });

        plugin.getLogger().info("Loaded " + formats.size() + " chat formats from config.");
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName,
                                     @NotNull Component message, @NotNull Audience viewer) {
        if (!(viewer instanceof Player)) {
            // If the viewer is not a player (e.g., console), we can just use the source as the viewer for PlaceholderAPI
            viewer = source;
        }
        final CachedMetaData user = luckPerms.getPlayerAdapter(Player.class).getMetaData(source);

        final String formatKey = user.getPrimaryGroup();
        if (formatKey == null || formatKey.isEmpty()) {
            plugin.getLogger().warning("Player " + source.getName() + " has no primary group set.");
            return Component.empty();
        }

        KyoriString format = formats.getOrDefault(formatKey, formats.get("default"));
        if (format == null) {
            plugin.getLogger().warning("Config does not contain a format for group " + formatKey + " and/or no \"default\" format is set.");
            return Component.empty();
        }

        if (usePlaceholderAPI) {
            String replacement = PlaceholderAPI.setPlaceholders(source, format.text());
            replacement = PlaceholderAPI.setRelationalPlaceholders(source, (Player) viewer, replacement);
            if (replaceLegacyAmpersand) {
                replacement = replaceLegacyAmpersand(replacement);
            }
            format = new KyoriString(replacement);
        }

        final List<Placeholder> placeholders = new ArrayList<>();
        String prefix = user.getPrefix() != null ? user.getPrefix() : "";
        String suffix = user.getSuffix() != null ? user.getSuffix() : "";

        if (replaceLegacyAmpersand) {
            prefix = replaceLegacyAmpersand(prefix);
            suffix = replaceLegacyAmpersand(suffix);
        }

        placeholders.add(Placeholder.of("prefix", prefix));
        placeholders.add(Placeholder.of("suffix", suffix));
        placeholders.add(Placeholder.of("message", MiniMessage.miniMessage().serialize(message)));
        placeholders.add(Placeholder.of("name", source.getName()));
        placeholders.add(Placeholder.of("group", formatKey));

        // Find meta tag references %meta-key% and replace them with their values
        for (Map.Entry<String, List<String>> meta : user.getMeta().entrySet()) {
            String key = meta.getKey();
            List<String> values = meta.getValue();
            if (values.isEmpty()) {
                continue;
            }
            // We only map the first value for each meta key
            String value = values.get(0);
            String placeholder = "%" + key + "%";
            placeholders.add(Placeholder.of(placeholder, value));
        }

        return format.replaceAndColor(placeholders.toArray(new Placeholder[0]));
    }

    private String replaceLegacyAmpersand(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        final Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        String result = MiniMessage.miniMessage().serialize(component);

        // un-escape adventure style tags \<tag> (LegacyComponentSerializer escapes them)
        return result.replace("\\<", "<");
    }
}
