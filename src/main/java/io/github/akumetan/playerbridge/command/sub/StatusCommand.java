package io.github.akumetan.playerbridge.command.sub;

import io.github.akumetan.playerbridge.PlayerBridge;
import io.github.akumetan.playerbridge.config.MessagesConfig;
import io.github.akumetan.playerbridge.notification.MessageFormatter;
import io.github.akumetan.playerbridge.profile.lookup.Fetcher;
import io.github.akumetan.playerbridge.profile.ownership.PlayerOwnership;
import io.github.akumetan.playerbridge.profile.ownership.PlayerOwnershipService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed;

public class StatusCommand implements SubCommand {

    private final PlayerBridge plugin;
    private final MessagesConfig config;
    private final PlayerOwnershipService service;
    private final Fetcher fetcher;

    public StatusCommand(PlayerBridge plugin, MessagesConfig config, PlayerOwnershipService service, Fetcher fetcher) {
        this.plugin = plugin;
        this.config = config;
        this.service = service;
        this.fetcher = fetcher;
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getAlias() {
        return "";
    }

    @Override
    public String getPermission() {
        return "playerbridge.command.status";
    }

    @Override
    public String getUsage() {
        return "<gray>/pb status <dark_gray><</dark_gray>player<dark_gray>></dark_gray></gray>";
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (args.length != 1) {
            MessageFormatter.sendFormatted(sender, this.getUsage());
            return;
        }

        String targetName = args[0];
        fetcher.lookupUUID(targetName).whenComplete((targetUUID, throwable) -> {
            if (throwable != null || targetUUID == null) {
                Bukkit.getScheduler().runTask(this.plugin, () -> MessageFormatter.sendFormatted(sender, this.config.checkVersion(), parsed("player", targetName)));
                return;
            }

            PlayerOwnership ownership;
            try {
                ownership = this.service.find(targetUUID);
            } catch (Exception exception) {
                Bukkit.getScheduler().runTask(this.plugin, () -> MessageFormatter.sendFormatted(sender, this.config.databaseError()));
                return;
            }

            Bukkit.getScheduler().runTask(this.plugin, () -> {
                MessageFormatter.sendHeader(sender, "Status");
                MessageFormatter.sendFormattedRaw(sender, "<gray>Player<dark_gray>:</dark_gray> <aqua>" + targetName + " <dark_gray>(</dark_gray>" + targetUUID + "<dark_gray>)</dark_gray></aqua></gray>");

                if (ownership == null) {
                    MessageFormatter.sendFormattedRaw(sender, "<gray>Ownership<dark_gray>:</dark_gray> <green>free</green></gray>");
                    return;
                }

                long secondsRemaining = Math.max(0, Duration.between(Instant.now(), ownership.expiresAt()).toSeconds());
                MessageFormatter.sendFormattedRaw(sender, "<gray>Ownership<dark_gray>:</dark_gray> <red>claimed</red></gray>");
                MessageFormatter.sendFormattedRaw(sender, "<gray>Server<dark_gray>:</dark_gray> <aqua>" + ownership.serverId() + "</aqua></gray>");
                MessageFormatter.sendFormattedRaw(sender, "<gray>Lease remaining<dark_gray>:</dark_gray> <aqua>" + secondsRemaining + "s</aqua></gray>");
            });
        });
    }


    @Override
    public List<String> getTabSuggestions(CommandSender sender, String[] args) {
        return List.of();
    }
}
