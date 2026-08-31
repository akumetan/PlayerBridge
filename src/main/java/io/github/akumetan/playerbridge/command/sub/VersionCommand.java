package io.github.akumetan.playerbridge.command.sub;

import io.github.akumetan.playerbridge.PlayerBridge;
import io.github.akumetan.playerbridge.config.MessagesConfig;
import io.github.akumetan.playerbridge.notification.MessageFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed;

public class VersionCommand implements SubCommand {

    private final PlayerBridge plugin;
    private final MessagesConfig msgConfig;

    public VersionCommand(PlayerBridge plugin, MessagesConfig msgConfig) {
        this.plugin = plugin;
        this.msgConfig = msgConfig;
    }

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public String getAlias() {
        return "ver";
    }

    @Override
    public String getPermission() {
        return "playerbridge.command.version";
    }

    @Override
    public String getUsage() {
        return "<gray>/pb version</gray>";
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        MessageFormatter.sendFormatted(sender, msgConfig.checkVersion(),
                parsed("version", this.plugin.getPluginMeta().getVersion()),
                parsed("server_version", Bukkit.getVersion()));
    }

    @Override
    public List<String> getTabSuggestions(CommandSender sender, String[] args) {
        return List.of();
    }
}
