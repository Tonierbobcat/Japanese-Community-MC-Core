package com.loficostudios.japaneseMinecraft;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathEffectParticles extends BukkitRunnable {
    private final long maxTicks;
    private final Player player;
    private long current = 0;

    public DeathEffectParticles(Player player, long maxTicks) {
        this.player = player;
        this.maxTicks = maxTicks;
    }

    @Override
    public void run() {
        if (current >= maxTicks) {
            this.cancel();
            return;
        }

        spawnDeathEffectParticles(player);
        current++;
    }

    private void spawnDeathEffectParticles(Player player) {
        Location loc = player.getLocation();

        Color from = Color.fromRGB(128, 0, 128);
        Color to = Color.fromRGB(238, 130, 238);

        Particle.DustTransition dust = new Particle.DustTransition(from, to, 1.5f);

        double yOffset = 1.0;

        for (int i = 0; i < 100; i++) {
            double radius = 0.6;

            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.random() * Math.PI;

            double x = loc.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double y = (loc.getY() + radius * Math.cos(phi)) + yOffset;
            double z = loc.getZ() + radius * Math.sin(phi) * Math.sin(theta);

            Location particleLoc = new Location(loc.getWorld(), x, y , z);
            loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, particleLoc, 1, dust);
        }
    }
}
