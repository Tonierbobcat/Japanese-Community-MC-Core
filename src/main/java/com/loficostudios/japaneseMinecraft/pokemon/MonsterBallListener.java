package com.loficostudios.japaneseMinecraft.pokemon;

import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.Items;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MonsterBallListener implements Listener {

    private final Map<UUID, BallThrow> monsterBalls = new HashMap<>();

    @EventHandler
    private void onThrow(ProjectileLaunchEvent e) {
        var projectile = e.getEntity();
        if (!(projectile instanceof Snowball))
            return;
        var shooter = e.getEntity().getShooter();
        if (!(shooter instanceof Player player))
            return;
        var main = Items.getItemFromItem(player.getInventory().getItemInMainHand(), MonsterBall.class);
        var off = Items.getItemFromItem(player.getInventory().getItemInOffHand(), MonsterBall.class);

        if (main != null) {
            monsterBalls.put(projectile.getUniqueId(), new BallThrow(main, player));
        }
        if (off != null) {
            monsterBalls.put(projectile.getUniqueId(), new BallThrow(main, player));
        }

        Debug.log("Projectile in map: " + monsterBalls.containsKey(projectile.getUniqueId()));
    }

    private record BallThrow(MonsterBall ball, Player whoThrow) {
    }

    @EventHandler
    private void onHit(ProjectileHitEvent e) {
        var hit = e.getHitEntity();
        var projectile = e.getEntity();

        if (!(projectile instanceof Snowball))
            return;

        var throwData = monsterBalls.remove(projectile.getUniqueId());
        if (throwData == null) {
            Debug.log("Projectile is not a monsterball!");
            return;
        }

        if (hit instanceof LivingEntity) {
//            ((LivingEntity) hit).damage(999);
            if (hit instanceof Player) {
                var jitem = Items.ITEMS.getById(throwData.ball().getId());
                projectile.getWorld().dropItem(projectile.getLocation(), Items.ITEMS.createItemStack(jitem));
                return;
            }

            Map<EntityType, Material> SPAWNEGGS = new HashMap<>();

            /// ANIMALS ONLY

            SPAWNEGGS.put(EntityType.COW, Material.COW_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.SHEEP, Material.SHEEP_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.PIG, Material.PIG_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.CHICKEN, Material.CHICKEN_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.HORSE, Material.HORSE_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.DONKEY, Material.DONKEY_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.MULE, Material.MULE_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.MOOSHROOM, Material.MOOSHROOM_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.LLAMA, Material.LLAMA_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.TRADER_LLAMA, Material.TRADER_LLAMA_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.OCELOT, Material.OCELOT_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.CAT, Material.CAT_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.RABBIT, Material.RABBIT_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.POLAR_BEAR, Material.POLAR_BEAR_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.TURTLE, Material.TURTLE_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.FOX, Material.FOX_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.WOLF, Material.WOLF_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.PANDA, Material.PANDA_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.PARROT, Material.PARROT_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.CAMEL, Material.CAMEL_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.DOLPHIN, Material.DOLPHIN_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.AXOLOTL, Material.AXOLOTL_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.FROG, Material.FROG_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.BAT, Material.BAT_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.TROPICAL_FISH, Material.TROPICAL_FISH_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.SALMON, Material.SALMON_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.COD, Material.COD_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.PUFFERFISH, Material.PUFFERFISH_SPAWN_EGG);
            SPAWNEGGS.put(EntityType.BEE, Material.BEE_SPAWN_EGG);

            var spawn = SPAWNEGGS.get(hit.getType());
            if (spawn == null) {
                return;
            }

            /// int Value between 0-10. 10 = 100%
            var captureRate = throwData.ball().getCapturePower();
            var rand = ThreadLocalRandom.current();

            double probability = captureRate / 10.0;
            boolean success = rand.nextDouble() < probability;
            if (success) {
                var loc = hit.getLocation();
                hit.remove();

                var captured = new ItemStack(spawn);
                var meta = captured.getItemMeta();
                if (meta != null) {
                    long timeCaptured = System.currentTimeMillis();
                    LocalDateTime dateTime = Instant.ofEpochMilli(timeCaptured)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
                    String formatted = dateTime.format(formatter);

                    meta.displayName(Component.text("Captured " + getEntityName(hit.getType()))
                            .decoration(TextDecoration.ITALIC, false));

                    /// Formated date yyyy/MM/dd HH:mm
                    meta.lore(List.of(
                            Component.text("§aCaught by " + throwData.whoThrow().getName() + " §7" + formatted)
                    ));

                    captured.setItemMeta(meta);
                }

                loc.getWorld().dropItem(loc, captured);
            }
        } else {
            var jitem = Items.ITEMS.getById(throwData.ball().getId());
            projectile.getWorld().dropItem(projectile.getLocation(), Items.ITEMS.createItemStack(jitem));
        }
    }

    private String getEntityName(EntityType type) {
        var builder = new StringBuilder();
        var name = type.name();

        var strings = name.split("_");
        for (String string : strings) {
            char[] chars = string.toCharArray();
            if (chars.length < 1)
                continue;
            chars[0] = Character.toUpperCase(chars[0]);
            builder.append(chars).append(" ");
        }

        return builder.toString().trim();
    }

}
