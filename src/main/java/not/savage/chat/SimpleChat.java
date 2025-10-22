package not.savage.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import not.savage.chat.commands.SimpleChatCommand;
import not.savage.chat.listeners.ChatMessageListener;
import not.savage.chat.listeners.DebugListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SimpleChat plugin for handling chat events in Minecraft.
 * Uses the latest API's and standards to ensure compatibility and performance.
 * MiniMessage & Signed-Chat support is included.
 */
public class SimpleChat extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Loading SimpleChat...");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        saveDefaultConfig();
        reloadConfig();

        new ChatMessageListener(this);
        new SimpleChatCommand(this);
        if (getConfig().getBoolean("debug", false)) {
            getLogger().info("Debug mode is enabled.");
            new DebugListener(this);
        }
        getLogger().info("SimpleChat is waiting for chat events...");
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleChat plugin is stopping...");
        AsyncChatEvent.getHandlerList().unregister(this);
    }
}
