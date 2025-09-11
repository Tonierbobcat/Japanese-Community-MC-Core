package com.loficostudios.japaneseMinecraft.pokemon;

import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.Items;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Range;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// TODO move some functionality to seperate classes
// TODO ADD TOP LEVEL DOCS
/// This class is the manager for monsterballs
public class MonsterBallListener implements Listener {

    private final int MAX_LEVEL = 99;
    private final int MIN_LEVEL = 1;
    private static final Map<EntityType, Material> SPAWN_EGGS;

    private final Map<UUID, BallThrow> monsterBalls = new HashMap<>();

    private final JapaneseMinecraft plugin;

    public MonsterBallListener(JapaneseMinecraft plugin) {
        this.plugin = plugin;

        JapaneseMinecraft.runTaskTimer(() -> {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (!SPAWN_EGGS.containsKey(entity.getType()))
                        continue;
                    if (!(entity instanceof LivingEntity))
                        continue;
                    if (entity instanceof Player)
                        continue;
                    updateEntityName(entity);
                }
            }
        }, 0, 10);
    }

    private void updateEntityName(Entity entity) {
        var wrapped = new MonsterWrapper(plugin, ((LivingEntity) entity));
        var owner = wrapped.getOwner();
        boolean hasOwner = owner != null;

        var level = wrapped.getLevel();
        if (level == null) {
            level = getRandomLevel();
            wrapped.setLevel(level);
        }

        var name = Common.formatEnumName(entity.getType());
        if (hasOwner) {
            entity.customName(Component.text(owner + "'s " + "Lvl " + level + " | " + name));
        } else {
            entity.customName(Component.text("Lvl " + level + " | " + name));
        }
        entity.setCustomNameVisible(true);
    }

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
            monsterBalls.put(projectile.getUniqueId(), new BallThrow(main, player, player.getInventory().getItemInMainHand()));
        }
        if (off != null) {
            monsterBalls.put(projectile.getUniqueId(), new BallThrow(off, player, player.getInventory().getItemInOffHand()));
        }

        Debug.log("Projectile in map: " + monsterBalls.containsKey(projectile.getUniqueId()));
    }

    private record BallThrow(MonsterBall ball, Player whoThrow, ItemStack stack) {
    }

    @EventHandler
    private void onSpawn(EntitySpawnEvent e) {
        var entity = e.getEntity();
        if (!(entity instanceof LivingEntity))
            return;
        var wrapped = new MonsterWrapper(plugin, ((LivingEntity) entity));
        var type = e.getEntityType();

        if (!SPAWN_EGGS.containsKey(type)) {
            return;
        }

        /// Check if entity does not have level we set it to a random level
        if (wrapped.getLevel() == null)
            wrapped.setLevel(getRandomLevel());

        //todo move this to player interact event
        //if the player is in creative remove the associated item from their inventory to prevent duplicate MONS
        if (wrapped.getMonsterBall() != null) {
            entity.getWorld().dropItem(entity.getLocation(), Items.ITEMS.createItemStack(wrapped.getMonsterBall()));
        }
    }

    private void initializeCatch(BallThrow throwData, MonsterWrapper entity) {
        var whoCaught = throwData.whoThrow();
        String originalOwner = entity.getOriginalOwner();
        if (originalOwner == null) {
            entity.setOriginalOwner(whoCaught);
        }
        entity.setOwner(whoCaught);
        if (entity.getDateCaught() == null)
            entity.setDateCaught(System.currentTimeMillis());
        entity.setMonsterBall(throwData.ball());
    }

    public int getRandomLevel() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        double r = rand.nextDouble();
        double power = 3.0;
        double biased = Math.pow(r, power);
        return MIN_LEVEL + (int) ((MAX_LEVEL - MIN_LEVEL) * biased);
    }

    @EventHandler
    private void onHit(ProjectileHitEvent e) {
        var hit = e.getHitEntity();
        var projectile = e.getEntity();

        if (!(projectile instanceof Snowball))
            return;

        var throwData = monsterBalls.remove(projectile.getUniqueId());
        if (throwData == null)
            return;

        if (!(hit instanceof LivingEntity) || hit instanceof Player) {
            spawnBallOnProjectile(throwData, projectile);
            return;
        }

        var spawn = getSpawnEggMaterial(hit.getType());
        if (spawn == null) {
            notifyPlayerOfInvalidCatch(throwData.whoThrow());
            spawnBallOnProjectile(throwData, projectile);
            return;
        }

        var wrapped = new MonsterWrapper(plugin, ((LivingEntity) hit));

        /// Validate data
        var entityLevel = wrapped.getLevel();

        /// Tell the thrower that this creature cannot be capture if entity has no level
        /// Useful for server entities
        if (entityLevel == null) {
            notifyPlayerOfInvalidCatch(throwData.whoThrow());
            spawnBallOnProjectile(throwData, projectile);
            return;
        }
        var entityName = Common.formatEnumName(hit.getType());

        /// No need to recalculate catch chances if the pokemon has already been caught
        var isCurrentOwner = throwData.whoThrow().getName().equals(Objects.requireNonNullElse(wrapped.getOwner(), ""));
        if (isCurrentOwner) {
            handleMonsterBallEnter(throwData, wrapped, entityLevel, entityName, spawn);
            throwData.whoThrow().sendMessage("You retrieved your " + entityLevel + " " + entityName);
            return;
        }

        /// int Value between 0-10. 10 = 100%
        var capturePower = throwData.ball().getCapturePower();
        var rand = ThreadLocalRandom.current();

        var attribute = ((LivingEntity) hit).getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = attribute != null ? attribute.getValue() : 20;

        double probability;
        if (throwData.ball().getId().equals(Items.MASTER_BALL.getId())) {
            probability = 1;
        } else {
            probability = calculateCatchProbability(capturePower, entityLevel, ((LivingEntity) hit).getHealth(), maxHealth);
        }

        boolean success = rand.nextDouble() < probability;
        if (!success) {
            notifyPlayerOfFailedCatch(throwData.whoThrow(), probability, ((LivingEntity) hit).getHealth(), maxHealth);
            return;
        }

        /// InitializeCatch before entering the pokeball
        initializeCatch(throwData, wrapped);

        handleMonsterBallEnter(throwData, wrapped, entityLevel, entityName, spawn);
    }


    @SuppressWarnings("UnstableApiUsage")
    private void handleMonsterBallEnter(BallThrow throwData, MonsterWrapper wrapped, int level, String name, Material spawn) {
        var loc = wrapped.getLocation();

        /// For whatever reason. date is null
        /// Date should not be null when catching a pokemon for the first time, and
        /// it should not be null when retrieving a pokemon.
        var date = wrapped.getDateCaught();
        if (date == null)
            wrapped.setDateCaught(System.currentTimeMillis());
        date = Objects.requireNonNull(wrapped.getDateCaught());

        var snapshot = wrapped.getEntity().createSnapshot();
        if (snapshot == null) {
            Debug.log("COULD NOT CREATE ENTITY SNAPSHOT");
            notifyPlayerOfInvalidCatch(throwData.whoThrow());
            return;
        }

        /// Remove entity after snapshot created
        wrapped.getEntity().remove();

        var captured = getCapturedItem(date, level, spawn, snapshot, throwData);

        loc.getWorld().dropItem(loc, captured);

        throwData.whoThrow().sendMessage("You caught a level " + level + " " + name);
    }

    /// [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Catch_rate)
    /// We are using the sword and shield generation for reference for the catch formula
    private double calculateCatchProbability(@Range(from = 0, to = 10) double ballCapturePower, double monLevel, double currentHealth, double maxHealth) {
        double heathFactor = ((3*maxHealth)-(2*currentHealth))/3*maxHealth;

        /// We are using the catch rate of caterpie as the default
        /// [キャタピー]https://bulbapedia.bulbagarden.net/wiki/Caterpie_(Pok%C3%A9mon)
        // catch rate is between 1 and 255
        int defaultMonCatchRate = 255;
        double rateModified = Math.max(1, Math.min(255, defaultMonCatchRate * ballCapturePower));
        var levelBonus = Math.max((30 - monLevel)/10, 1);

        return 1; //todo change this
    }

    /// used for when the ball needs to be drops at where it hit
    /// also used for spawning the ball to reclaim it
    private void spawnBallOnProjectile(BallThrow throwData, Projectile projectile) {
        var jitem = Items.ITEMS.getById(throwData.ball().getId());
        projectile.getWorld().dropItem(projectile.getLocation(), Items.ITEMS.createItemStack(jitem));
    }

    private void notifyPlayerOfFailedCatch(Player player, double probability, double currentHealth, double maxHealth) {
        player.sendMessage("You failed to catch this MON. " + (probability * 100) + "% " + currentHealth + "/" + maxHealth + "HP");
    }

    private void notifyPlayerOfInvalidCatch(Player player) {
        player.sendMessage("Cannot catch this MON");
    }

    private Material getSpawnEggMaterial(EntityType type) {
        /// ANIMALS ONLY
        return SPAWN_EGGS.get(type);
    }

    //TODO there is a really wierd back the the item that you used to throw the ball does not have metadata
    private ItemStack getCapturedItem(long date, int level, Material spawnEgg, EntitySnapshot snapshot, BallThrow throwData) {
        var captured = new ItemStack(spawnEgg);
        var meta = captured.getItemMeta();
        var spawnEggMeta = ((SpawnEggMeta) meta);

        spawnEggMeta.setSpawnedEntity(snapshot);

        /// format data
        LocalDateTime dateTime = Instant.ofEpochMilli(date)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        String formatted = dateTime.format(formatter);

        /// Rather than having the name be "Captured {type}" we will just set the stack name as the ball
//        meta.displayName(Component.text("Captured " + entityName)
//                .decoration(TextDecoration.ITALIC, false));

        var stack = throwData.stack();
        var throwMeta = stack.getItemMeta();
        if (throwMeta != null) {
            meta.displayName(throwMeta.displayName());
        }

        /// Formated date yyyy/MM/dd HH:mm
        meta.lore(List.of(
                Component.text("§fLevel: " + level), //surround with objects.requireNonNull
                Component.text("§aCaught by " + throwData.whoThrow().getName()),
                Component.text("§7Date: " + formatted)
        ));

        var pdc = meta.getPersistentDataContainer();

        /// RATHER THAN STORING THE BALL ON THE ITEM WE STORE IT ON THE ENTITY
//        pdc.set(ballKey, PersistentDataType.STRING, throwData.ball().getId());

        /// Add random pdc do make the stack unique if caught with the same ball at the same time
        var garbage = UUID.randomUUID()
                .toString();
        pdc.set(new NamespacedKey(garbage, "dummy"), PersistentDataType.BYTE, (byte) 1);

        captured.setItemMeta(meta);
        return captured;
    }

    static {
        /// Rather than storing the spawm material in the map.
        /// store a list of allowed mobs and then get the material by Material#valueOf(EntityType#name() + _SPAWN_EGG)
        SPAWN_EGGS = new HashMap<>();

        SPAWN_EGGS.put(EntityType.COW, Material.COW_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.SHEEP, Material.SHEEP_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.PIG, Material.PIG_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.CHICKEN, Material.CHICKEN_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.HORSE, Material.HORSE_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.DONKEY, Material.DONKEY_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.MULE, Material.MULE_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.MOOSHROOM, Material.MOOSHROOM_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.LLAMA, Material.LLAMA_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.TRADER_LLAMA, Material.TRADER_LLAMA_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.OCELOT, Material.OCELOT_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.CAT, Material.CAT_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.RABBIT, Material.RABBIT_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.POLAR_BEAR, Material.POLAR_BEAR_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.TURTLE, Material.TURTLE_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.FOX, Material.FOX_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.WOLF, Material.WOLF_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.PANDA, Material.PANDA_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.PARROT, Material.PARROT_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.CAMEL, Material.CAMEL_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.DOLPHIN, Material.DOLPHIN_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.AXOLOTL, Material.AXOLOTL_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.FROG, Material.FROG_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.BAT, Material.BAT_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.TROPICAL_FISH, Material.TROPICAL_FISH_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.SALMON, Material.SALMON_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.COD, Material.COD_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.PUFFERFISH, Material.PUFFERFISH_SPAWN_EGG);
        SPAWN_EGGS.put(EntityType.BEE, Material.BEE_SPAWN_EGG);
    }
}
