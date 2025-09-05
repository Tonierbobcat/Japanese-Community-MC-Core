package com.loficostudios.japaneseMinecraft.listener;

import com.loficostudios.japaneseMinecraft.DeathEffectParticles;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.Messages;
import com.loficostudios.japaneseMinecraft.cooldown.Cooldown;
import com.loficostudios.japaneseMinecraft.cooldown.SimpleCooldown;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerDeathListener implements Listener {
    private static final int EFFECT_DURATION_TICKS = 60;

    private final Map<UUID, BukkitTask> deathTasks = new HashMap<>();

    private final JapaneseMinecraft plugin;

    private final Cooldown cannotPvpMessageCooldown = new SimpleCooldown(2*1000);
    
    public PlayerDeathListener(JapaneseMinecraft plugin) {
        this.plugin = plugin;
    }

    private void playReviveEffect(Player player) {
        var maxHealth = player.getAttribute(Attribute.MAX_HEALTH);

        player.setExp(0);
        player.setLevel(0);

        player.setHealth(maxHealth != null ? maxHealth.getValue() : 20.0);

        notifyPlayerOfRevive(player);

        var existingTask = deathTasks.get(player.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
        }

        startParticleTask(player);

        /// Modify player states
        player.setInvulnerable(true);

        Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(plugin, player));

        /// Wrapping in optional to avoid writing null checks
        Optional.ofNullable(player.getAttribute(Attribute.MOVEMENT_SPEED)).ifPresent((instance) -> {
            try {
                instance.addModifier(new AttributeModifier(JapaneseMinecraft.getNMK("death_speed_boost"), 0.8, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
            } catch (IllegalArgumentException ignored) {
            }
        });

        JapaneseMinecraft.runTaskLater(() -> resetPlayer(player), PlayerDeathListener.EFFECT_DURATION_TICKS);
    }

    private void notifyPlayerOfRevive(Player player) {
        player.sendMessage(Messages.getMessage(player, "player_revived"));
        player.sendMessage(Messages.getMessage(player, "you_lost_experience"));

        player.playSound(player, Sound.ENTITY_CAT_HURT, 0.5f, 1);
        player.playSound(player, Sound.ITEM_TOTEM_USE, 0.4f, 1);
    }

    private void startParticleTask(Player player) {
        int particleInterval = 4;
        var particles = new DeathEffectParticles(player, EFFECT_DURATION_TICKS / particleInterval);
        deathTasks.put(player.getUniqueId(), JapaneseMinecraft.runTaskTimer(particles,
                0, particleInterval));
    }

    private void resetPlayer(Player player) {
        /// Unsetting modified player states
        player.setInvulnerable(false);

        Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, player));

        /// Wrapping in optional to avoid writing null checks
        Optional.ofNullable(player.getAttribute(Attribute.MOVEMENT_SPEED)).ifPresent((instance) -> {
            instance.removeModifier(JapaneseMinecraft.getNMK("death_speed_boost"));
        });
    }

    private int getLives(Player player) {
        return JapaneseMinecraft.getPlayerProfile(player).getLives();
    }

    private void setLives(Player player, int lives) {
        var profile = JapaneseMinecraft.getPlayerProfile(player);
        profile.setLives(lives);
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent e) {
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setDroppedExp(0);
    }

    @EventHandler
    private void onRespawn(PlayerRespawnEvent e) {
        setLives(e.getPlayer(), 3);
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player))
            return;
        var causing = e.getDamageSource().getCausingEntity();
        var causedByPlayer = causing instanceof Player;
        if (causedByPlayer) {
            e.setCancelled(true);
            if (cannotPvpMessageCooldown.has(causing.getUniqueId())) {
                causing.sendMessage(Messages.getMessage(((Player) causing), "cannot_pvp"));
            }
            return;
        }

        boolean isDeath = e.getFinalDamage() >= player.getHealth();
        if (!isDeath)
            return;

        var canRevive = getLives(player) > 1;
        if (canRevive) {
            setLives(player, getLives(player) - 1);
        } else {
            return;
        }
        e.setCancelled(true);
        playReviveEffect(player);
    }
}
