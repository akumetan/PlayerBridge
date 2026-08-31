package io.github.akumetan.playerbridge.command;

import io.github.akumetan.playerbridge.command.sub.SubCommand;
import io.github.akumetan.playerbridge.config.MessagesConfig;
import io.github.akumetan.playerbridge.notification.MessageFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PlayerBridgeCommand implements CommandExecutor {

    private final CommandManager cmdManager;
    private final MessagesConfig msgConfig;

    public PlayerBridgeCommand(CommandManager cmdManager, MessagesConfig msgConfig) {
        this.cmdManager = cmdManager;
        this.msgConfig = msgConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            cmdManager.sendUsagePage(sender);
            return true;
        }

        SubCommand subCommand = cmdManager.find(args[0]);
        if (subCommand == null) {
            cmdManager.sendUsagePage(sender);
            return true;
        }
        if (!sender.hasPermission(subCommand.getPermission())) {
            MessageFormatter.sendFormatted(sender, msgConfig.noPermission());
            return true;
        }

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);
        subCommand.run(sender, subArgs);
        return true;
    }

}
