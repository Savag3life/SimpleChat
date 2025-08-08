package not.savage.chat.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import not.savage.chat.adventure.SimpleChatRenderer;
import not.savage.chat.SimpleChat;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listeners for handling incoming chat messages, replacing the renderer as needed.
 */
public class ChatMessageListener implements Listener {

    private final SimpleChat plugin;
    private final SimpleChatRenderer renderer;

    public ChatMessageListener(final SimpleChat plugin) {
        this.plugin = plugin;
        renderer = new SimpleChatRenderer(plugin);

        // Allow people to change the event priority via config - so they can fix overriding issues themselves (if necessary)
        final EventPriority priority = switch (plugin.getConfig().getString("priority", "NORMAL").toUpperCase()) {
            case "LOWEST" -> EventPriority.LOWEST;
            case "LOW" -> EventPriority.LOW;
            case "HIGH" -> EventPriority.HIGH;
            case "HIGHEST" -> EventPriority.HIGHEST;
            case "MONITOR" -> EventPriority.MONITOR;
            default -> EventPriority.NORMAL;
        };
        plugin.getServer().getPluginManager()
                .registerEvent(
                        AsyncChatEvent.class,
                        this,
                        priority,
                        (listener, event) -> onPlayerChat((AsyncChatEvent) event),
                        plugin
                );

        plugin.getLogger().info("Registered chat message listener with priority: " + priority.name());
    }

    // No @EventHandler annotation here since we use an EventExecutor to handle the event
    public void onPlayerChat(AsyncChatEvent event) {
        if (!event.isAsynchronous()) {
            plugin.getLogger().info("Failed to format chat message, event is not asynchronous.");
            return;
        }
        event.renderer(this.renderer);
    }
}
