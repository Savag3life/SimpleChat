package not.savage.chat.listeners;

import io.papermc.paper.event.player.AbstractChatEvent;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import not.savage.chat.adventure.SimpleChatRenderer;
import not.savage.chat.SimpleChat;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Method;

/**
 * Listeners for handling incoming chat messages, replacing the renderer as needed.
 */
public class ChatMessageListener implements Listener {

    private final SimpleChat plugin;
    private final SimpleChatRenderer renderer;

    public ChatMessageListener(final SimpleChat plugin) {
        this.plugin = plugin;
        renderer = new SimpleChatRenderer(plugin);

        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("Debug mode enabled, printing registered listeners for chat events.");
            printListeners(AsyncChatEvent.class);
            printListeners(ChatEvent.class);
            printListeners(PlayerChatEvent.class);
            printListeners(AsyncPlayerChatEvent.class);
        }

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

    private void printListeners(Class<? extends Event> eventClass) {
        try {
            // Every Event has a public static HandlerList getHandlerList()
            Method getHandlerListMethod = eventClass.getMethod("getHandlerList");
            HandlerList handlerList = (HandlerList) getHandlerListMethod.invoke(null);

            if (handlerList.getRegisteredListeners().length == 0) {
                plugin.getLogger().info("No listeners registered for event: " + eventClass.getSimpleName());
                return;
            }

            for (RegisteredListener registered : handlerList.getRegisteredListeners()) {
                Object listenerInstance = registered.getListener();
                Class<?> listenerClass = listenerInstance.getClass();
                String pluginName = registered.getPlugin().getName();
                EventPriority priority = registered.getPriority();
                plugin.getLogger().info("Listener: " + listenerClass.getName() + ", Plugin: " + pluginName + ", Priority: " + priority.name());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
