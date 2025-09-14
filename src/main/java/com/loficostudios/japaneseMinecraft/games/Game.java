package com.loficostudios.japaneseMinecraft.games;

import com.loficostudios.japaneseMinecraft.Messages;
import org.bukkit.entity.Player;

import java.util.function.Function;

public interface Game {

    int getLengthMinutes();

    void reset();

    void end();

    void start();

    int getMinPlayers();

    boolean isActive();

    String getId();

    String getPrefix();

    default void notifyPlayer(Player player, String key, Function<String, String> replacer) {
        player.sendMessage(getPrefix() + replacer.apply(Messages.getMessage(player, key)));
    }

    default void notifyPlayer(Player player, String message) {
        player.sendMessage(getPrefix() + message);
    }
}
