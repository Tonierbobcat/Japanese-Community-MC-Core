package com.loficostudios.japaneseMinecraft.games.shiritori;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShiritoriManager implements Listener {
    private static final int GAME_LENGTH_MINUTES = 2;

    private static final int AUTO_START_MINUTES = 25;

    private ShiritoriGame game;

    public ShiritoriManager(JapaneseMinecraft plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        JapaneseMinecraft.runTaskTimer(this::tryStartNewGame, 20 * 60, 20 * 60 * AUTO_START_MINUTES);
    }

    public boolean tryStartNewGame() {
        if (game != null || Bukkit.getOnlinePlayers().size() < 2)
            return false;

        game = new ShiritoriGame(GAME_LENGTH_MINUTES);
        game.start();

        JapaneseMinecraft.runTaskLater(this::endGame, 20 * 60 * 2); //
        return true;
    }

    private void endGame() {
        if (game != null) {
            var tmp = game;
            game = null;

            var results = tmp.getResults();

            var message = getResultsMessage(results);

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message.replace("{points}", "" + results.getOrDefault(player.getUniqueId(), 0)));
            }

            int fifthPlaceMoney = 100;
            int fourthPlaceMoney = 100;
            int thirdPlaceMoney = 100;
            int secondPlaceMoney = 300;
            int firstPlaceMoney = 750;

            Map<Integer, Integer> moneyRewards = Map.of(
                    0, firstPlaceMoney,
                    1, secondPlaceMoney,
                    2, thirdPlaceMoney,
                    3, fourthPlaceMoney,
                    4, fifthPlaceMoney
            );

            int index = 0;
            for (Map.Entry<UUID, Integer> entry : results.entrySet()) {
                var reward = moneyRewards.get(index);
                if (reward != null) {
                    var eng = ShiritoriGame.PREFIX + "You have received a ${money} reward!"
                            .replace("{money}", "" + reward);
                    var player = Bukkit.getPlayer(entry.getKey());
                    if (player != null) {
                        player.sendMessage(eng);
                    }
                }
                index++;
            }
        }
    }

    private String getResultsMessage(Map<UUID, Integer> results) {
        int playersToDisplay = 5;
        int index = 0;
        List<String> lines = new ArrayList<>();
        lines.add("§a§l=== Shiritori Results ===");
        for (Map.Entry<UUID, Integer> entry : results.entrySet()) {
            var player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline())
                continue;
            if (index < playersToDisplay) {
                lines.add("§e{rank}. §f{name} §6- §e{points} point(s)"
                        .replace("{rank}", "" + (index + 1))
                        .replace("{name}", player.getName())
                        .replace("{points}", "" + entry.getValue()));
                index++;
            } else {
                break;
            }
        }
        lines.add("§6You scored §e{points}§6 point(s)!");
        lines.add("§a§l=====================");

        return String.join("\n", lines);
    }

    @EventHandler
    private void onChat(AsyncChatEvent e) {
        if (game == null)
            return;

        /// run it off the thread that called the event
        JapaneseMinecraft.runTaskAsynchronously(() -> {
            var message = PlainTextComponentSerializer.plainText().serialize(e.message());
            game.submitWord(e.getPlayer(), message);
        });
    }
}
