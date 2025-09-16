package com.loficostudios.japaneseMinecraft.sanity;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


// todo finish/fix this
public class SanityManager {
    private static final long TICK_SPEED = 30;
    private static final double MIN_DISTANCE_FROM_PLAYER = 100;

    /// slightly higher than decrease per second
    private static final double PER_PLAYER_INCREASE = 0.2;

    /// This might get crazy
    private static final double AMOUNT_TO_DECREASE_PER_TICK = 0.1;

    /// the idea is that sanity is always decrease but there are always going to be ways of slowing it down
    public SanityManager() {
        JapaneseMinecraft.runTaskTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                var profile = JapaneseMinecraft.getPlayerProfile(player);

                var total = 0.0;

                total += getAmountToIncrease(player);
                total -= getAmountToDecrease(player);

                profile.setSanity(Math.max(profile.getSanity() + total, 0));
            }
        }, 0, TICK_SPEED); ///EVERY second to save performance
    }

    private double getAmountToIncrease(Player player) {
        /// Check distance to other players
        var nearby = player.getNearbyEntities(MIN_DISTANCE_FROM_PLAYER, MIN_DISTANCE_FROM_PLAYER, MIN_DISTANCE_FROM_PLAYER).stream()
                .filter(a -> a instanceof Player)
                .map(a -> (Player) a).toList();

        var players = nearby.size();
        var inTown = JapaneseMinecraft.getTownsAPI().getTownContainer().getTown(player.getLocation()) != null;
        return players * PER_PLAYER_INCREASE + (inTown ? 0.05 : 0);
    }

    private double getAmountToDecrease(Player player) {
        double decreaseMultiplier = 1;
        boolean totalDarkness = player.getLocation().getBlock().getLightLevel() <= 0;

        boolean isHungry = player.getFoodLevel() <= 6;

        if (isHungry)
            decreaseMultiplier++;
        if (totalDarkness)
            decreaseMultiplier++;

        return totalDarkness ? AMOUNT_TO_DECREASE_PER_TICK * decreaseMultiplier : AMOUNT_TO_DECREASE_PER_TICK;
    }
}
