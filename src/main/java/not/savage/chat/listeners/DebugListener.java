package not.savage.chat.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import not.savage.chat.SimpleChat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class DebugListener implements Listener {

    private final SimpleChat plugin;

    public DebugListener(SimpleChat plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkCancelled(AsyncChatEvent event) {
        if (event.isCancelled()) {
            plugin.getLogger().info("AsyncChatEvent was cancelled!");
        } else {
            plugin.getLogger().info("AsyncChatEvent was not cancelled.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void checkCancelledMonitor(AsyncChatEvent event) {
        if (event.isCancelled()) {
            plugin.getLogger().info("AsyncChatEvent was cancelled in MONITOR phase!");
        } else {
            plugin.getLogger().info("AsyncChatEvent was not cancelled in MONITOR phase.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectRenderer(AsyncChatEvent event) {
        plugin.getLogger().info("LOWEST priority renderer: " + event.renderer().getClass().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectRendererAfter(AsyncChatEvent event) {
        plugin.getLogger().info("AFTER event renderer: " + event.renderer().getClass().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectRenderer(AsyncPlayerChatEvent event) {
        plugin.getLogger().info("LOWEST priority format: " + event.getFormat());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectRendererAfter(AsyncPlayerChatEvent event) {
        plugin.getLogger().info("AFTER event format: " + event.getFormat());
    }
}
