package io.github.akumetan.playerbridge.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class PlayerBridgeTabCompleter implements TabCompleter {

    private final CommandManager cmdManager;

    public PlayerBridgeTabCompleter(CommandManager cmdManager) {
        this.cmdManager = cmdManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return cmdManager.getBaseTabCompletions(sender, args);
    }
}