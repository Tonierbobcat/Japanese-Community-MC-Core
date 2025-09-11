package com.loficostudios.japaneseMinecraft.notifications;

import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;

public class NotificationManager {

    /// mapping types to prefixes
    private static final Map<Notification.Type, String> TYPE_PREFIX = Map.of(
        Notification.Type.BOUNTY, Common.createMessagePrefix("Bounty", "§e"),
        Notification.Type.ANNOUNCEMENT, Common.createMessagePrefix("Announcement", "§a"),
        Notification.Type.ALERT, Common.createMessagePrefix("Alert", "§c"),
        Notification.Type.INFO, ""
    );

    public void sendNotification(Notification notification, Player... players) {
        for (Player player : players) {
            String message = switch (JapaneseMinecraft.getPlayerProfile(player).getLanguage()) {
                case JAPANESE -> notification.jp();
                case ENGLISH -> notification.eng();
            };
            var prefix = TYPE_PREFIX.getOrDefault(notification.type(), "");

            /// handling prefix separately to avoid parsing issues
            player.sendMessage(Component.text(prefix).append(JapaneseMinecraft.parseText(player, message)));
        }
    }
}
