package com.loficostudios.japaneseMinecraft.games.shiritori;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ShiritoriListener implements Listener {
    private final ShiritoriGame game = new ShiritoriGame();

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        game.start();
    }

    @EventHandler
    private void onChat(AsyncChatEvent e) {
        var message = PlainTextComponentSerializer.plainText().serialize(e.message());
        game.submitWord(e.getPlayer(), message);
    }
}
