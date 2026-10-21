package io.github.akumetan.playerbridge.command.sub;

import io.github.akumetan.playerbridge.config.MessagesConfig;
import io.github.akumetan.playerbridge.gui.Editor;
import io.github.akumetan.playerbridge.gui.EditorService;
import io.github.akumetan.playerbridge.notification.MessageFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditCommand implements SubCommand {

    private final EditorService service;
    private final MessagesConfig config;

    public EditCommand(EditorService service, MessagesConfig config) {
        this.service = service;
        this.config = config;
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getAlias() {
        return "";
    }

    @Override
    public String getPermission() {
        return "playerbridge.command.edit";
    }

    @Override
    public String getUsage() {
        return "<gray>/pb edit <dark_gray><</dark_gray>player<dark_gray>></dark_gray> <dark_gray><</dark_gray>inv<dark_gray>|</dark_gray>ec<dark_gray>|</dark_gray>armor<dark_gray>></dark_gray></gray>";
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageFormatter.sendFormatted(sender, this.config.playerOnlyCommand());
            return;
        }

        if (args.length != 2) {
            MessageFormatter.sendFormatted(sender, this.getUsage());
            return;
        }

        String target = args[0];

        switch (args[1].toLowerCase()) {
            case "inv", "inventory" -> {
                this.service.open(player, target, Editor.STORAGE);
            }
            case "ec", "enderchest" -> {
                this.service.open(player, target, Editor.ENDER_CHEST);
            }
            case "armor" -> {
                this.service.open(player, target, Editor.ARMOR);
            }
            default -> {
                MessageFormatter.sendFormatted(sender, this.getUsage());
            }
        }

    }

    @Override
    public List<String> getTabSuggestions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> suggestions.add(player.getName()));

            StringUtil.copyPartialMatches(args[0], suggestions, completions);
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("inv");
            suggestions.add("inventory");
            suggestions.add("ec");
            suggestions.add("enderchest");
            suggestions.add("armor");

            StringUtil.copyPartialMatches(args[0], suggestions, completions);
            return completions;
        }

        return completions;
    }
}
