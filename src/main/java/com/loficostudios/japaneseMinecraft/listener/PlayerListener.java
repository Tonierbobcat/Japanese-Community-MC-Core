package com.loficostudios.japaneseMinecraft.listener;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.Messages;
import com.loficostudios.japaneseMinecraft.WeatherManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import java.util.Map;

public class PlayerListener implements Listener {

    private static final String JOIN_MESSAGE = "§a§l+ §f<player>";
    private static final String QUIT_MESSAGE = "§c§l- §f<player>";

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

        /// run a tick later so that the profile loads
        JapaneseMinecraft.runTaskLater(() -> {
            sendWelcomeMessage(player);
        }, 1);
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

    private void sendWelcomeMessage(Player player) {
        player.sendMessage(JapaneseMinecraft.parseText(player, Messages.getMessage(player, "welcome_message")));
    }

    private void updateDisplay(Player player) {
        var profile = JapaneseMinecraft.getPlayerProfile(player);
        var world = player.getWorld();
        var time = world.getTime();
        var shifted = (time + 6000) % 24000;
        var timeText = "§e⌚ §f" + String.format("%02d:%02d", shifted / 1000, ((shifted % 1000) * 60) / 1000);
        var livesText = "§a❤ §f" + profile.getLives();

        Map<WeatherManager.WeatherType, String> symbols = Map.of(
                WeatherManager.WeatherType.SUNNY, world.isDayTime() ? "§e☀" : "§f☽",
                WeatherManager.WeatherType.CLOUDY, "§7☁",
                WeatherManager.WeatherType.STORMY, "§e⚡",
                WeatherManager.WeatherType.RAINY, "§9☂",
                WeatherManager.WeatherType.SNOWY, "§b❄"
        );

        var type = plugin.getWeatherManager().getWeatherType(player);

        var symbol = symbols.getOrDefault(type, "<null>");

        /// make sure that money is shorten for example 1.5k or 105.2m max digits = 5
        /// Will have a /bal command to get the exact balance
        var moneyText = "§9$ §f" + profile.getMoney();

        /// Maybe it changes the color depending on the value
        var sanityText = "§c☯ §f" + String.format("%.2f", profile.getSanity()) + "%";

        var weatherText = symbol + " §f" + plugin.getWeatherManager().getTemperature(player) + "°C";

        player.sendActionBar(Component.text(livesText + " §r| " +
                sanityText + " §r| " +
                moneyText + " §r| " +
                timeText + " §r| " +
                weatherText));
    }
}
