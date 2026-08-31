package io.github.akumetan.playerbridge.command.sub;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    String getName();

    String getAlias();

    String getPermission();

    String getUsage();

    void run(CommandSender sender, String[] args);

    List<String> getTabSuggestions(CommandSender sender, String[] args);
}
