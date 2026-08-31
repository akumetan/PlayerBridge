package io.github.akumetan.playerbridge.notification;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

public class MessageFormatter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Component PREFIX = MINI_MESSAGE.deserialize("<dark_gray>[</dark_gray><gradient:#00F2FE:#4FACFE>PlayerBridge</gradient><dark_gray>]</dark_gray> ");

    public static Component format(String rawTemplate, TagResolver... resolvers) {
        return PREFIX.append(MINI_MESSAGE.deserialize(rawTemplate, resolvers));
    }

    public static void sendFormatted(CommandSender sender, String rawTemplate, TagResolver... resolvers) {
        sender.sendMessage(PREFIX.append(MINI_MESSAGE.deserialize(rawTemplate, resolvers)));
    }

    public static Component formatRaw(String rawTemplate, TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(rawTemplate, resolvers);
    }

    public static void sendFormattedRaw(CommandSender sender, String rawTemplate, TagResolver... resolvers) {
        sender.sendMessage(MINI_MESSAGE.deserialize(rawTemplate, resolvers));
    }
}
