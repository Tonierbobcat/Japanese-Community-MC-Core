package com.loficostudios.japaneseMinecraft.pokemon;

import com.loficostudios.japaneseMinecraft.*;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
public class MonsterBallListener implements Listener { //todo maybe?? rename to somethingManager

    private final int MAX_LEVEL = 99;
    private final int MIN_LEVEL = 1;
    private static final Set<EntityType> CAPTURABLE_ENTITIES;

    private final Map<UUID, BallThrow> monsterBalls = new HashMap<>();

    private final JapaneseMinecraft plugin;

    private final NamespacedKey ballKey;

    private final Map<UUID, ItemStack> spawnItems = new HashMap<>();

    public MonsterBallListener(JapaneseMinecraft plugin) {
        this.plugin = plugin;

        /// hehe 'balls'
        ballKey = new NamespacedKey(plugin, "balls");

        JapaneseMinecraft.runTaskTimer(() -> {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (!isMonsterMon(entity))
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
        var mainHand = player.getInventory().getItemInMainHand();
        var offHand = player.getInventory().getItemInOffHand();
        var mainBall = Items.getItemFromItem(player.getInventory().getItemInMainHand(), MonsterBall.class);
        var offBall = Items.getItemFromItem(player.getInventory().getItemInOffHand(), MonsterBall.class);

        if (mainBall != null) {
            Debug.log( "MainHand Type: " + mainHand.getType() +" MainHand has ItemMeta: " + mainHand.hasItemMeta());
            monsterBalls.put(projectile.getUniqueId(), new BallThrow(mainBall, player));
        }
        if (offBall != null) {
            Debug.log( "OffHand Type: " + offHand.getType() +" OffHand has ItemMeta: " + offHand.hasItemMeta());
            monsterBalls.put(projectile.getUniqueId(), new BallThrow(offBall, player));
        }

        Debug.log("Projectile in map: " + monsterBalls.containsKey(projectile.getUniqueId()));
    }

    private record BallThrow(MonsterBall ball, Player whoThrew) {
    }

    private boolean isMonsterMon(Entity entity) {
        return (entity instanceof LivingEntity) && !(entity instanceof Player) && CAPTURABLE_ENTITIES.contains(entity.getType());
    }

    @EventHandler
    private void onSpawn(EntitySpawnEvent e) {
        var entity = e.getEntity();
        if (!isMonsterMon(entity))
            return;

        /// we can safely cast entity to living entity
        var wrapped = new MonsterWrapper(plugin, ((LivingEntity) entity));

        /// Check if entity does not have level we set it to a random level
        if (wrapped.getLevel() == null)
            wrapped.setLevel(getRandomLevel());

        var ownerUUID = wrapped.getOwnerUUID();

        ItemStack spawnItem = ownerUUID != null ? spawnItems.remove(ownerUUID) : null;

        /// if the mon was spawned without an item return
        if (spawnItem == null)
            return;
        var onlineOwner = Bukkit.getPlayer(ownerUUID);

        /// if the player is in creative remove the associated item from their inventory to prevent duplicate MONS
        if (onlineOwner != null && onlineOwner.getGameMode().equals(GameMode.CREATIVE)) {
            spawnItem.setAmount(spawnItem.getAmount() - 1);
        }

        /// drop the ball
        if (wrapped.getMonsterBall() != null) {
            entity.getWorld().dropItem(entity.getLocation(), Items.ITEMS.createItemStack(wrapped.getMonsterBall()));
        }
    }

    private void initializeCatch(BallThrow throwData, MonsterWrapper entity) {
        var whoCaught = throwData.whoThrew();
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
        Player whoThrew = throwData.whoThrew();

        if (hit == null) {
            spawnBallOnProjectile(throwData, projectile);
            return;
        }

        if (!isMonsterMon(hit)) {
            notifyPlayerOfInvalidCatch(whoThrew);
            spawnBallOnProjectile(throwData, projectile);
        }

        var wrapped = new MonsterWrapper(plugin, ((LivingEntity) hit));

        /// Validate data
        var entityLevel = wrapped.getLevel();

        /// Tell the thrower that this creature cannot be capture if entity has no level
        /// Useful for server entities
        if (entityLevel == null) {
            notifyPlayerOfInvalidCatch(whoThrew);
            spawnBallOnProjectile(throwData, projectile);
            return;
        }
        var entityName = Common.formatEnumName(hit.getType());

        /// No need to recalculate catch chances if the pokemon has already been caught
        var isCurrentOwner = whoThrew.getName().equals(Objects.requireNonNullElse(wrapped.getOwner(), ""));
        var isOwned = wrapped.getOwner() != null;
        if (isCurrentOwner) {
            handleMonsterBallEnter(throwData, wrapped, entityLevel);
            var message = Messages.getMessage(whoThrew, "retrieved_creature");
            whoThrew.sendMessage(message
                    .replace("{level}", "" + entityLevel)
                    .replace("{name}", entityName));
            return;

        ///if is owned but player catching is not owner
        } else if (isOwned) {
            var message = Messages.getMessage(whoThrew, "cannot_catch_others_creatures");
            whoThrew.sendMessage(message);
            spawnBallOnProjectile(throwData, projectile);
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
        Debug.log("Catch Probability: " + probability);

        boolean success = rand.nextDouble() < probability;
        if (!success) {
            var message = Messages.getMessage(whoThrew, "failed_catch");
            whoThrew.sendMessage(message.replace("{probability}", "" + ((probability * 100))));
            return;
        }

        /// InitializeCatch before entering the pokeball
        initializeCatch(throwData, wrapped);

        handleMonsterBallEnter(throwData, wrapped, entityLevel);

        var message = Messages.getMessage(whoThrew, "creature_caught");
        whoThrew.sendMessage(message
                .replace("{level}", "" + entityLevel)
                .replace("{name}", entityName));
    }

    @EventHandler
    private void onInteract(PlayerInteractAtEntityEvent e) {
        var player = e.getPlayer();
        var item = player.getInventory().getItem(e.getHand());
        if (Items.isItem(item, Items.LEVEL_CANDY)) {
            var entity = e.getRightClicked();
            if (!isMonsterMon(entity))
                return;

            var wrapped = new MonsterWrapper(plugin, ((LivingEntity) entity));

            /// Check if it is owned by the player first
            if (wrapped.getOwner() == null || !wrapped.getOwner().equals(player.getName())) {
                player.sendMessage(Messages.getMessage(player, "item_level_candy_not_owned"));
                return;
            }

            var level = wrapped.getLevel();
            if (level == null)
                return;
            if (level >= MAX_LEVEL) {
                player.sendMessage(Messages.getMessage(player, "item_level_candy_max_level"));
                return;
            }

            item.setAmount(item.getAmount() - 1);
            wrapped.setLevel(wrapped.getLevel() + 1);
            player.playSound(entity.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            player.sendMessage(Messages.getMessage(player, "item_level_candy_levelup"));
        }
    }

    /// lowest here so that it can be cancelled
    @EventHandler(priority = EventPriority.LOWEST)
    private void onInteract(PlayerInteractEvent e) {
        /// spawn eggs can only be placed on blocks
        if (e.isCancelled() || !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        var item = e.getItem();
        if (item == null || item.getType().equals(Material.AIR) || !item.hasItemMeta())
            return;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        var ballId = pdc.get(ballKey, PersistentDataType.STRING);
        if (ballId == null)
            return;
        spawnItems.put(e.getPlayer().getUniqueId(), item);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void handleMonsterBallEnter(BallThrow throwData, MonsterWrapper wrapped, int level) {
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
            notifyPlayerOfInvalidCatch(throwData.whoThrew());
            return;
        }

        /// Remove entity after snapshot created
        wrapped.getEntity().remove();

        var captured = getCapturedItem(date, level, getSpawnEggMaterial(wrapped.getType()), snapshot, throwData);

        loc.getWorld().dropItem(loc, captured);
    }

    /// [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Catch_rate)
    /// We are using the sword and shield generation for reference for the catch formula
    /**
     *
     * @return a value from 0.0 - 1.0
     */
    private double calculateCatchProbability(@Range(from = 0, to = 10) double ballCapturePower, double monLevel, double currentHealth, double maxHealth) {
        /// We are using the catch rate of caterpie as the default
        /// [キャタピー]https://bulbapedia.bulbagarden.net/wiki/Caterpie_(Pok%C3%A9mon)
        /// catch rate is between 1 and 255
        int defaultMonCatchRate = 255;

        double healthFactor = (3.0 * maxHealth - 2.0 * currentHealth) / (3.0 * maxHealth);
        double rateModified = Math.max(1, Math.min(255, defaultMonCatchRate));
        double levelBonus = Math.max(1.0, (30.0 - monLevel) / 10.0);

        double a = healthFactor * 4096.0 * rateModified * ballCapturePower * levelBonus;

        return Math.min(1.0, a / 65536.0);
    }

    /// used for when the ball needs to be drops at where it hit
    /// also used for spawning the ball to reclaim it
    private void spawnBallOnProjectile(BallThrow throwData, Projectile projectile) {
        var jitem = Items.ITEMS.getById(throwData.ball().getId());
        projectile.getWorld().dropItem(projectile.getLocation(), Items.ITEMS.createItemStack(jitem));
    }

    private void notifyPlayerOfInvalidCatch(Player player) {
        player.sendMessage(Messages.getMessage(player, "uncatchable_creature"));
    }

    /**
     *
     * @throws IllegalArgumentException if it cannot retrieve material
     */
    private Material getSpawnEggMaterial(EntityType type) {
        return Material.valueOf(type.name() + "_SPAWN_EGG");
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

        /// Create a fresh instance of the item
        var stack = Items.ITEMS.createItemStack(throwData.ball);
        /// set max stack to 1
        stack.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        var throwMeta = stack.getItemMeta();
        if (throwMeta != null) {
            meta.displayName(throwMeta.displayName());
        }


        /// Formated date yyyy/MM/dd HH:mm
        meta.lore(List.of(
                Component.text("§fLevel: " + level), //surround with objects.requireNonNull
                Component.text("§aCaught by " + throwData.whoThrew().getName()),
                Component.text("§7Date: " + formatted)
        ));

        var pdc = meta.getPersistentDataContainer();

        // store ball on item
        pdc.set(ballKey, PersistentDataType.STRING, throwData.ball().getId());

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
        CAPTURABLE_ENTITIES = Set.of(
                EntityType.COW,
                EntityType.SHEEP,
                EntityType.PIG,
                EntityType.CHICKEN,
                EntityType.HORSE,
                EntityType.DONKEY,
                EntityType.MULE,
                EntityType.MOOSHROOM,
                EntityType.LLAMA,
                EntityType.TRADER_LLAMA,
                EntityType.OCELOT,
                EntityType.CAT,
                EntityType.RABBIT,
                EntityType.POLAR_BEAR,
                EntityType.TURTLE,
                EntityType.FOX,
                EntityType.WOLF,
                EntityType.PANDA,
                EntityType.PARROT,
                EntityType.CAMEL,
                EntityType.DOLPHIN,
                EntityType.AXOLOTL,
                EntityType.FROG,
                EntityType.BAT,
                EntityType.TROPICAL_FISH,
                EntityType.SALMON,
                EntityType.COD,
                EntityType.PUFFERFISH,
                EntityType.BEE
        );
    }
}
