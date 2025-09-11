package com.loficostudios.japaneseMinecraft.sanity;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SanityManager {
    private static final double MIN_DISTANCE_FROM_PLAYER = 100;

    private static final double PER_PLAYER_INCREASE = 1;

    /// This might get crazy
    private static final double AMOUNT_TO_DECREASE_PER_SECOND = 0.1;

    public SanityManager() {
        JapaneseMinecraft.runTaskTimer(() -> {
            for (World world : Bukkit.getWorlds()) {
                for (Player player : world.getPlayers()) {

                    // Check distance to other players
                    var nearby = player.getNearbyEntities(MIN_DISTANCE_FROM_PLAYER, MIN_DISTANCE_FROM_PLAYER, MIN_DISTANCE_FROM_PLAYER).stream()
                            .filter(a -> a instanceof Player)
                            .map(a -> (Player) a).toList();

                    var players = nearby.size();

                    if (players > 0) {
                        var amountToIncrease = players * PER_PLAYER_INCREASE;
                        //increase sanity
                    } else {
                        //decrease sanity
                    }
                }
            }
        }, 0, 20); ///EVERY second to save performance
    }
}
