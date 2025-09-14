package com.loficostudios.japaneseMinecraft.notifications;

import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

//todo the automated messaging is a bit finicky
public class NotificationManager {

    private static final Map<Notification.Type, Long> INTERVALS_SECONDS = Map.of(
            Notification.Type.BOUNTY, 220L,
            Notification.Type.ANNOUNCEMENT, 300L,
            Notification.Type.ALERT, 300L,
            Notification.Type.INFO, 120L
    );

    /// mapping types to prefixes
    private static final Map<Notification.Type, String> TYPE_PREFIX = Map.of(
        Notification.Type.BOUNTY, Common.createMessagePrefix("Bounty", "§e"),
        Notification.Type.ANNOUNCEMENT, Common.createMessagePrefix("Announcement", "§a"),
        Notification.Type.ALERT, Common.createMessagePrefix("Alert", "§c"),

        /// INFO as not prefix
        Notification.Type.INFO, ""
    );

    private final Map<Notification.Type, List<Notification>> notifications = new HashMap<>();
    private final Map<Notification.Type, Integer> nextIndex = new EnumMap<>(Notification.Type.class);

    public NotificationManager(JapaneseMinecraft plugin) {
        loadNotifications(plugin);

//        for (Notification.Type type : Notification.Type.values()) {
//            if (notifications.getOrDefault(type, List.of()).isEmpty())
//                continue;
//            start(type);
//        }
    }

    private void loadNotifications(JapaneseMinecraft plugin) {
        File file = new File(plugin.getDataFolder(), "notifications.yaml");
        if (!file.exists()) {
            if(plugin.getResource("notifications.yaml") == null) {
                try {
                    file.createNewFile();
                } catch (IOException ignored) {
                }
            }else {
                plugin.saveResource("notifications.yaml", false);
            }
        }

        var config = YamlConfiguration.loadConfiguration(file);

        for (String id : config.getKeys(false)) {
            String rawType = config.getString(id + ".type", "")
                    .toUpperCase();
            String englishMessage = config.getString(id + ".english");
            String japaneseMessage = config.getString(id + ".japanese");

            double weight = config.getDouble(id + ".weight", 1.0);

            Notification.Type type = Notification.Type.valueOf(rawType);

            var notification = new Notification(englishMessage, japaneseMessage, type, weight);
            this.notifications.compute(type, (t, l) -> {
                if (l != null) {
                    l.add(notification);
                    return l;
                }

                List<Notification> list = new LinkedList<>();
                list.add(notification);
                return list;
            });
        }
    }

    /// the notifications that are automated should ideally be INFO, ANNOUNCEMENTS, BOUNTIES
    private void start(Notification.Type type) {
        JapaneseMinecraft.runTaskTimer(() -> {
            var notifications = this.notifications.getOrDefault(type, List.of());
            if (notifications.isEmpty())
                return;

            double totalWeight = notifications.stream()
                    .mapToDouble(Notification::weight)
                    .sum();

            /// the info send gets picked weighted randomly
            if (Objects.requireNonNull(type) == Notification.Type.INFO) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    double random = ThreadLocalRandom.current().nextDouble(totalWeight);
                    double cumulative = 0;
                    Notification chosen = notifications.getFirst();
                    for (Notification n : notifications) {

                        cumulative += n.weight();
                        if (random <= cumulative) {
                            chosen = n;
                            break;
                        }
                    }

                    sendNotification(chosen, player);
                }
                return;
            }

            int index = nextIndex.getOrDefault(type, 0);
            var notification = notifications.get(index);

            for (Player player : Bukkit.getOnlinePlayers()) {
                sendNotification(notification, player);
            }

            nextIndex.put(type, (index + 1) % notifications.size());
        }, ThreadLocalRandom.current().nextInt(0, 120) * 20L, INTERVALS_SECONDS.get(type) * 20L);
    }

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
