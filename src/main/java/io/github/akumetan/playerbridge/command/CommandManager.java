package io.github.akumetan.playerbridge.command;

import io.github.akumetan.playerbridge.command.sub.SubCommand;
import io.github.akumetan.playerbridge.notification.MessageFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final List<SubCommand> subCommands = new ArrayList<>();

    public void register(SubCommand command) {
        subCommands.add(command);
    }

    public SubCommand find(String name) {
        for (SubCommand command : subCommands) {
            if (command.getName().equalsIgnoreCase(name) || command.getAlias() != null && command.getAlias().equalsIgnoreCase(name))
                return command;
        }
        return null;
    }

    public void sendUsagePage(CommandSender sender) {
        for (SubCommand command : subCommands) {
            sender.sendMessage(MessageFormatter.format(command.getUsage()));
        }
    }

    public List<String> getBaseTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                suggestions.add(subCommand.getName());
                if (subCommand.getAlias() != null && !subCommand.getAlias().isEmpty())
                    suggestions.add(subCommand.getAlias());
            }

            StringUtil.copyPartialMatches(args[0], suggestions, completions);
            return completions;
        }

        SubCommand subCommand = find(args[0]);
        if (subCommand != null) {
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, subArgs.length);
            return subCommand.getTabSuggestions(sender, subArgs);
        }

        return completions;
    }
}
