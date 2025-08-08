package not.savage.chat.adventure;

import io.papermc.paper.chat.ChatRenderer;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import not.savage.chat.SimpleChat;
import not.savage.chat.util.KyoriString;
import not.savage.chat.util.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Viewer UnAware Chat Renderer for SimpleChat.
 */
public class SimpleChatRenderer implements ChatRenderer, ChatRenderer.ViewerUnaware {

    private final SimpleChat plugin;
    private final LuckPerms luckPerms;
    private final Map<String, KyoriString> formats = new HashMap<>();
    private final boolean usePlaceholderAPI;

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
        plugin.getLogger().info("PlaceholderAPI is " + (usePlaceholderAPI ? "enabled, placeholders will be processed in message format."
                : "not enabled, placeholders will not be processed."));

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
        return render(source, sourceDisplayName, message);
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message) {
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
            format = new KyoriString(PlaceholderAPI.setPlaceholders(source, format.text()));
        }

        final Component messageFormatted = format.replaceAndColor(
                Placeholder.of("name", source.getName()),
                Placeholder.of("group", formatKey),
                Placeholder.of("prefix", user.getPrefix() != null ? user.getPrefix() : ""),
                Placeholder.of("suffix", user.getSuffix() != null ? user.getSuffix() : "")
        );

        return Component.text().append(messageFormatted).append(message).build();
    }
}
