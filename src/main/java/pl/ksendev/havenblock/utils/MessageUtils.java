package pl.ksendev.havenblock.utils;

import org.bukkit.command.CommandSender;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;


public class MessageUtils {
    
    public static final String PREFIX_RAW = "<b><dark_gray>[</dark_gray><gradient:#C1E540:#15EEC2>HavenBlock</gradient><dark_gray>]</dark_gray></b>";
    private static final Component PREFIX_COMPONENT = MiniMessage.miniMessage().deserialize(PREFIX_RAW);

    public static void sendMessage(CommandSender sender, String message, boolean prefixed) {
        Component content = MiniMessage.miniMessage().deserialize(message);
        Component fullMessage;

        if (prefixed) {
            fullMessage = Component.empty()
                        .append(PREFIX_COMPONENT)
                        .append(Component.text(" "))
                        .append(content);
        } else {
            fullMessage = content;
        }

        sender.sendMessage(fullMessage);
    }

}
