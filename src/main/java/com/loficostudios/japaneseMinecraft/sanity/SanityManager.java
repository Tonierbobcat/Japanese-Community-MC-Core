package com.loficostudios.japaneseMinecraft.sanity;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;


// todo finish/fix this
public class SanityManager {
    private static final double MIN_DISTANCE_FROM_PLAYER = 100;

    /// slightly higher than decrease per second
    private static final double PER_PLAYER_INCREASE = 0.2;

    /// This might get crazy
    private static final double AMOUNT_TO_DECREASE_PER_SECOND = 0.1;

    public SanityManager() {
        JapaneseMinecraft.runTaskTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                var profile = JapaneseMinecraft.getPlayerProfile(player);
                // Check distance to other players
                var nearby = player.getNearbyEntities(MIN_DISTANCE_FROM_PLAYER, MIN_DISTANCE_FROM_PLAYER, MIN_DISTANCE_FROM_PLAYER).stream()
                        .filter(a -> a instanceof Player)
                        .map(a -> (Player) a).toList();

                var players = nearby.size();

                // todo maybe?? if there a tone of players sanity could go to 200% or 300%
                if (players > 0) {
                    //increase sanity
                    var amountToIncrease = players * PER_PLAYER_INCREASE;
                    profile.setSanity(profile.getSanity() + amountToIncrease);
                } else {
                    //decrease sanity
                    // todo fix this. it is not working

                    double multiplier = 1;

                    boolean totalDarkness = player.getLocation().getBlock().getLightLevel() <= 0;

                    boolean isHungry = player.getFoodLevel() <= 6;

                    if (isHungry)
                        multiplier++;
                    if (totalDarkness)
                        multiplier++;

                    var amountToDecrease = totalDarkness ? AMOUNT_TO_DECREASE_PER_SECOND * multiplier : AMOUNT_TO_DECREASE_PER_SECOND;
                    profile.setSanity(Math.max(profile.getSanity() - amountToDecrease, 0));
                }
            }
        }, 0, 20); ///EVERY second to save performance
    }
}
