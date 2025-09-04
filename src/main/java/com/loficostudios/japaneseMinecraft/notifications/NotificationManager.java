package com.loficostudios.japaneseMinecraft.notifications;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;

public class NotificationManager {

    private static final Map<Notification.Type, String> TYPE_PREFIX = Map.of(
        Notification.Type.BOUNTY, "§8[§eBounty§8] ",
        Notification.Type.ANNOUNCEMENT, "§8[§aAnnouncement§8] ",
        Notification.Type.ALERT, "§8[§cAlert§8] ",
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
