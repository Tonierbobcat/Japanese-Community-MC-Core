package com.loficostudios.japaneseMinecraft.games.shiritori;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ShiritoriManager implements Listener {
    private static final int GAME_LENGTH_MINUTES = 2;

    private ShiritoriGame game;

    private final JapaneseMinecraft plugin;

    public ShiritoriManager(JapaneseMinecraft plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                tryStartNewGame();
            }
        }.runTaskTimer(plugin, 20 * 60, 20 * 60 * 5); // every 5 minutes
    }

    private void tryStartNewGame() {
        if (game != null)
            return;

        game = new ShiritoriGame(GAME_LENGTH_MINUTES);
        game.start();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (game != null) {
                    var tmp = game;
                    game = null;

                    var results = tmp.getResults();

                    int playersToDisplay = 5;
                    int index = 0;

                    List<String> lines = new ArrayList<>();
                    lines.add("§a§l=== Shiritori Results ===");
                    for (Map.Entry<UUID, Integer> entry : results.entrySet()) {
                        var player = Bukkit.getPlayer(entry.getKey());
                        if (player == null || !player.isOnline())
                            continue;
                        if (index < playersToDisplay) {
                            lines.add("§e" + (index + 1) + ". §f" + player.getName() + " §6- §e" + entry.getValue() + " point(s)");
                            index++;
                        } else {
                            break;
                        }
                    }
                    lines.add("§6You scored §e{points}§6 point(s)!");
                    lines.add("§a§l=======================");

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(String.join("\n", lines).replace("{points}", "" + results.getOrDefault(player.getUniqueId(), 0)));
                    }
                }
            }
        }.runTaskLater(plugin, 20 * 60 * 2); // 2 minutes
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        tryStartNewGame(); // DEBUG
    }

    @EventHandler
    private void onChat(AsyncChatEvent e) {
        var message = PlainTextComponentSerializer.plainText().serialize(e.message());
        game.submitWord(e.getPlayer(), message);
    }
}
