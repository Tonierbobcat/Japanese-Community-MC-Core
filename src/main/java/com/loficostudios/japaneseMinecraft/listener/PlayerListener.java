package com.loficostudios.japaneseMinecraft.listener;

import com.loficostudios.japaneseMinecraft.items.Items;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.WeatherManager;
import com.loficostudios.japaneseMinecraft.notifications.Notification;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlayerListener implements Listener {

    private static final String JOIN_MESSAGE = "§a§l+ §f<player>";
    private static final String QUIT_MESSAGE = "§c§l- §f<player>";

    private static final List<Notification> NOTIFICATIONS;

    private final BossBar overlay;

    private final JapaneseMinecraft plugin;

    public PlayerListener(JapaneseMinecraft plugin) {
        this.plugin = plugin;

        String[] lines = {
                "!!! JOIN NOW !!!",
                "@ " + JapaneseMinecraft.SERVER_IP,
                "!!! DISCORD !!!",
                "@ " + JapaneseMinecraft.DISCORD_URL
        };

        overlay = Bukkit.createBossBar(lines[0], BarColor.BLUE, BarStyle.SOLID);

        int[] index = {0};
        JapaneseMinecraft.runTaskTimer(() -> {
            overlay.setTitle(lines[index[0]]);
            index[0] = (index[0] + 1) % lines.length;
        }, 0, 45);

        JapaneseMinecraft.runTaskTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateDisplay(player);
            }
        }, 0, 2);
    }

    @EventHandler
    private void onServerPing(ServerListPingEvent e) {;
        e.motd(Component.text(JapaneseMinecraft.MOTD));
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        e.quitMessage(Component.text(QUIT_MESSAGE.replace("<player>", e.getPlayer().getName())));
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();

        e.joinMessage(Component.text(JOIN_MESSAGE.replace("<player>", player.getName())));

        overlay.addPlayer(player);

        if (player.getGameMode().equals(GameMode.SURVIVAL))
            player.setInvulnerable(false);
        Optional.ofNullable(player.getAttribute(Attribute.MOVEMENT_SPEED)).ifPresent((instance) -> {
            instance.removeModifier(JapaneseMinecraft.getNMK("death_speed_boost"));
        });

        try {
            player.getInventory().addItem(Items.createGiftBag(null, null));
        } catch (IllegalArgumentException ignore) {
        }

        // TODO Move these to a NotificationManager
        JapaneseMinecraft.runTaskLater(() -> {

            if (NOTIFICATIONS.isEmpty())
                return;

            player.sendMessage(" ");

            plugin.getNotificationManager().sendNotification(NOTIFICATIONS.getFirst(), player);

            int[] index = {1};
            JapaneseMinecraft.runTaskTimer((runnable) -> {

                final int max = NOTIFICATIONS.size();
                if (index[0] >= max) {
                    runnable.cancel();
                    return;
                }

                player.sendMessage(" ");
                plugin.getNotificationManager().sendNotification(NOTIFICATIONS.get(index[0]), player);

                index[0]++;
            }, 20L*10L, 20L*10L);
        }, 30L);
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent e) {
        /// Prevents farmland from being trampled
        if (e.getAction().equals(Action.PHYSICAL)) {
            Block block = e.getClickedBlock();
            if(block != null && block.getType().equals(Material.FARMLAND))
                e.setCancelled(true);
        }
    }

    private void updateDisplay(Player player) {
        var world = player.getWorld();
        var time = world.getTime();
        var shifted = (time + 6000) % 24000;
        var timeText = "§e⌚ §f" + String.format("%02d:%02d", shifted / 1000, ((shifted % 1000) * 60) / 1000);
        var livesText = "§a❤ §f" + JapaneseMinecraft.getPlayerProfile(player).getLives();

        Map<WeatherManager.WeatherType, String> symbols = Map.of(
                WeatherManager.WeatherType.SUNNY, world.isDayTime() ? "§e☀" : "§f☽",
                WeatherManager.WeatherType.CLOUDY, "§7☁",
                WeatherManager.WeatherType.STORMY, "§e⚡",
                WeatherManager.WeatherType.RAINY, "§9☂",
                WeatherManager.WeatherType.SNOWY, "§b❄"
        );

        var type = plugin.getWeatherManager().getWeatherType(player);

        var symbol = symbols.getOrDefault(type, "<null>");

        player.sendActionBar(Component.text(livesText + " §r| " + timeText + " §r| " + symbol + " §f" + plugin.getWeatherManager().getTemperature(player) + "°C"));
    }

    static {
        String[] englishWelcomeMessage = {
                "Welcome, {player}, to the JP-ENG community server!",
                "This server is a work in progress. Features may be added or changed over time.",
                "If you have any suggestions, please use /jpmc suggest <suggestion>",
                "Enjoy your time here!",
        };
        String[] japaneseWelcomeMessage = {
                "ようこそ、%player%さん、JP-ENGコミュニティサーバーへ！",
                "このサーバーは進行中のプロジェクトです。機能は時間とともに追加または変更される場合があります。",
                "ご提案がございましたら、/jpmc suggest <提案> をご利用ください。",
                "ここでの時間をお楽しみください！",
                " ",
                " - 開発者注記。あなたが望むかもしれない機能をコード化/追加することができます。"
        };

        NOTIFICATIONS = List.of(
                new Notification(
                        String.join("\n", englishWelcomeMessage),
                        String.join("\n", japaneseWelcomeMessage),
                        Notification.Type.INFO),
                new Notification(
                        "This is an open source project! Check out the code, report issues, or contribute on GitHub!\n<aqua>Github: <click:open_url:{github_url}>Japanese-Community-MC-Core</click>",
                        "これはオープンソースプロジェクトです！コードを確認したり、問題を報告したり、GitHubで貢献したりしてください！",
                        Notification.Type.INFO),
                new Notification(
                        "Looking for someone to draw a 64x64 server icon. Apply on the Discord!",
                        "64x64のサーバーアイコンを描いてくれる人を探しています。Discordで応募してください！",
                        Notification.Type.BOUNTY)
        );
    }
}
