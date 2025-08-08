package not.savage.chat.commands;

import io.papermc.paper.event.player.AsyncChatEvent;
import not.savage.chat.util.KyoriString;
import not.savage.chat.SimpleChat;
import not.savage.chat.listeners.ChatMessageListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SimpleChatCommand implements CommandExecutor {

    private final SimpleChat plugin;

    public SimpleChatCommand(final SimpleChat plugin) {
        this.plugin = plugin;
        plugin.getCommand("chatreload").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.hasPermission("simplechat.reload")) {
            plugin.reloadConfig();
            AsyncChatEvent.getHandlerList().unregister(plugin);
            new ChatMessageListener(plugin);
            commandSender.sendMessage(KyoriString.of(plugin.getConfig().getString("messages.reloaded")).color());
            return true;
        } else {
            commandSender.sendMessage(KyoriString.of(plugin.getConfig().getString("messages.no_permission")).color());
            return false;
        }
    }
}
