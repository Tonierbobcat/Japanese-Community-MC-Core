package com.loficostudios.japaneseMinecraft.pokemon;

import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MonsterBallListener implements Listener {

    private final Map<UUID, MonsterBall> monsterBalls = new HashMap<>();

    @EventHandler
    private void onThrow(ProjectileLaunchEvent e) {
        var projectile = e.getEntity();
        if (!(projectile instanceof Snowball))
            return;
        var shooter = e.getEntity().getShooter();
        if (!(shooter instanceof Player player))
            return;
        var main = getMonsterBall(player.getInventory().getItemInMainHand());
        var off = getMonsterBall(player.getInventory().getItemInOffHand());
        if (main != null) {
            monsterBalls.put(projectile.getUniqueId(), main);
        }
        if (off != null) {
            monsterBalls.put(projectile.getUniqueId(), off);
        }

        Debug.log("Projectile in map: " + monsterBalls.containsKey(projectile.getUniqueId()));
    }

    private MonsterBall getMonsterBall(ItemStack item) {
        var type = item.getType();
        if (type.equals(Material.AIR) || !item.hasItemMeta()) {
            Bukkit.getLogger().info("TYPE is equal to air or item does not have meta");
            return null;
        }

        var meta = Objects.requireNonNull(item.getItemMeta());
        var pdc = meta.getPersistentDataContainer();

        var id = pdc.get(Items.ITEMS.getItemKey(), PersistentDataType.STRING);
        if (id == null) {
            return null;
        }

        var i = Items.ITEMS.getById(id);
        if (!(i instanceof MonsterBall))
            return null;
        return ((MonsterBall) i);
    }

    @EventHandler
    private void onHit(ProjectileHitEvent e) {
        var hit = e.getHitEntity();
        var projectile = e.getEntity();

        if (!(projectile instanceof Snowball))
            return;

        var monsterBall = monsterBalls.remove(projectile.getUniqueId());
        if (monsterBall == null) {
            Debug.log("Projectile is not a monsterball!");
            return;
        }

        if (hit instanceof LivingEntity) {
//            ((LivingEntity) hit).damage(999);
            if (hit instanceof Player) {
                var jitem = Items.ITEMS.getById(monsterBall.getId());
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

            /// int Value between 0-10. 10 = 100%
            var captureRate = monsterBall.getCapturePower();
            var rand = ThreadLocalRandom.current();

            double probability = captureRate / 10.0;
            boolean success = rand.nextDouble() < probability;
            if (success) {
                var spawn = SPAWNEGGS.get(hit.getType());
                if (spawn != null) {
                    var loc = hit.getLocation();
                    hit.remove();

                    loc.getWorld().dropItem(loc, new ItemStack(spawn));
                }
            }
        } else {
            var jitem = Items.ITEMS.getById(monsterBall.getId());
            projectile.getWorld().dropItem(projectile.getLocation(), Items.ITEMS.createItemStack(jitem));
        }
    }
}
