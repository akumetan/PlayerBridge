package io.github.akumetan.playerbridge.command.sub;

import io.github.akumetan.playerbridge.config.ConfigManager;
import io.github.akumetan.playerbridge.notification.MessageFormatter;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand implements SubCommand {

    private final ConfigManager cfgManager;

    public ReloadCommand(ConfigManager cfgManager) {
        this.cfgManager = cfgManager;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getAlias() {
        return "rl";
    }

    @Override
    public String getPermission() {
        return "playerbridge.command.reload";
    }

    @Override
    public String getUsage() {
        return "<gray>/pb reload</gray>";
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        cfgManager.loadConfiguration();
        MessageFormatter.sendFormatted(sender, cfgManager.getConfig().messages().reloadConfig());
    }

    @Override
    public List<String> getTabSuggestions(CommandSender sender, String[] args) {
        return List.of();
    }
}
