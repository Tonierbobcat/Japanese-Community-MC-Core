package com.loficostudios.monstermons;

import com.loficostudios.forgified.paper.items.JItem;
import com.loficostudios.forgified.registry.Registries;
import com.loficostudios.forgified.registry.RegistryObject;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
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
public class MonsterBallManager {

    private final int MAX_LEVEL = 99;
    private final int MIN_LEVEL = 1;
    private static final Set<EntityType> CAPTURABLE_ENTITIES;

    private final Map<UUID, BallThrow> monsterBalls = new HashMap<>();

    private final MonsterMonsPlugin plugin;

    private final NamespacedKey ballKey;

//    private final Map<UUID, ItemStack> spawnItems = new HashMap<>();

    private final JItem levelCandyItem;

    public MonsterBallManager(MonsterMonsPlugin plugin) {
        this.plugin = plugin;

        this.levelCandyItem = Registries.ITEM.getById(plugin.getConfig().getString("level-candy-item-id"));

        /// hehe 'balls'
        ballKey = new NamespacedKey(plugin, "balls");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (!isMonsterMon(entity)) {
                            entity.customName(null);
                            entity.setCustomNameVisible(false);
                            continue;
                        }
                        updateEntityName(entity);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    private void updateEntityName(Entity entity) {
        var monsterData = getMonsterData(entity);
        if (monsterData == null)
            return;

        var owner = monsterData.owner();

        var name = entity.getType();
        var genderText = monsterData.getGender().equals(MonsterGender.MALE) ? Component.text("♂", NamedTextColor.BLUE) : Component.text("♀", NamedTextColor.RED);
        if (owner != null) {
            entity.customName(Component.text(owner.getName() + "'s " + "Lv. " + monsterData.level() + " | " + name).appendSpace().append(genderText));
        } else {
            entity.customName(Component.text("Lv. " + monsterData.level() + " | " + name).appendSpace().append(genderText));
        }
        entity.setCustomNameVisible(true);
    }

    public void handleThrow(Player player, Projectile projectile) {
        var mainBall = Registries.ITEM.get(player.getInventory().getItemInMainHand(), MonsterBall.class);
        var offBall = Registries.ITEM.get(player.getInventory().getItemInOffHand(), MonsterBall.class);

        if (mainBall != null) {
            monsterBalls.put(projectile.getUniqueId(), new BallThrow(mainBall, player));
        }
        if (offBall != null) {
            monsterBalls.put(projectile.getUniqueId(), new BallThrow(offBall, player));
        }
    }

    private record BallThrow(MonsterBall ball, Player whoThrew) {
    }

    private boolean isMonsterMon(Entity entity) {
        return (entity instanceof LivingEntity) && !(entity instanceof Player) && CAPTURABLE_ENTITIES.contains(entity.getType());
    }

    public void handleSpawn(Entity entity) {
        if (!isMonsterMon(entity))
            return;

        var existing = getMonsterData(entity);
        if (existing == null) {
            existing = new MonsterData(null, null, entity.getName(), entity.getType(), 0, getRandomLevel(), MonsterGender.values()[ThreadLocalRandom.current().nextInt(MonsterGender.values().length)]);
            setMonsterData(entity, existing);
        }
    }

    private void initializeCatch(BallThrow throwData, MonsterData monsterData) {

        var whoCaught = throwData.whoThrew();
        if (!monsterData.isWild())
            throw new IllegalArgumentException();
        monsterData.originalOwner(whoCaught);
        monsterData.owner(whoCaught);
        monsterData.dataCaught(System.currentTimeMillis());
    }

    public int getRandomLevel() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        double r = rand.nextDouble();
        double power = 3.0;
        double biased = Math.pow(r, power);
        return MIN_LEVEL + (int) ((MAX_LEVEL - MIN_LEVEL) * biased);
    }

    private @Nullable BallThrow dispose(Projectile projectile) {
        return monsterBalls.remove(projectile.getUniqueId());
    }

    public void handleBallHit(Projectile projectile, Entity hit) {
        if (!(projectile instanceof Snowball))
            return;

        var throwData = dispose(projectile);
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
            return;
        }

        var monsterData = getMonsterData(hit);
        if (monsterData == null) {
            notifyPlayerOfInvalidCatch(whoThrew);
            spawnBallOnProjectile(throwData, projectile);
            return;
        }

        /// Validate data
        var entityLevel = monsterData.level();

        var entityName = hit.getType().name();

        /// No need to recalculate catch chances if the pokemon has already been caught
        var owner = monsterData.owner();
        var hasOwner = owner != null;
        var isCurrentOwner = hasOwner && owner.getUniqueId().equals(whoThrew.getUniqueId());
        if (isCurrentOwner) {
            handleMonsterBallEnter(throwData, hit, monsterData);
            var message = getMessage(whoThrew, "retrieved_creature");
            whoThrew.sendMessage(message
                    .replace("{level}", "" + entityLevel)
                    .replace("{name}", entityName));
            return;

            ///if is owned but player catching is not owner
        } else if (hasOwner) {
            var message = getMessage(whoThrew, "cannot_catch_others_creatures");
            whoThrew.sendMessage(message);
            spawnBallOnProjectile(throwData, projectile);
            return;
        }

        /// int Value between 0-10. 10 = 100%
        var capturePower = throwData.ball().getCapturePower();
        var rand = ThreadLocalRandom.current();

        var attribute = ((LivingEntity) hit).getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = attribute != null ? attribute.getValue() : 20;

        var probability = calculateCatchProbability(capturePower, entityLevel, ((LivingEntity) hit).getHealth(), maxHealth);

        boolean success = rand.nextDouble() < probability;
        if (!success) {
            var message = getMessage(whoThrew, "failed_catch");
            whoThrew.sendMessage(message.replace("{probability}", "" + ((probability * 100))));
            return;
        }

        /// InitializeCatch before entering the pokeball
        initializeCatch(throwData, monsterData);

        setMonsterData(hit, monsterData);

        handleMonsterBallEnter(throwData, hit, monsterData);

        var message = getMessage(whoThrew, "creature_caught");
        whoThrew.sendMessage(message
                .replace("{level}", "" + entityLevel)
                .replace("{name}", entityName));
    }

    public MonsterData getMonsterData(Entity entity) {
        var config = new PersistentDataContainerConfig(entity.getPersistentDataContainer(), null);
        var key = "monsterdata";
        if (!config.has(key))
            return null;
        try {
            return config.get(key, MonsterData.class);
        } catch (Exception e) {
            e.printStackTrace();
            var data = new MonsterData(null, null, entity.getName(), entity.getType(), 0, getRandomLevel(), MonsterGender.values()[ThreadLocalRandom.current().nextInt(MonsterGender.values().length)]);
            config.set(key, data);
            return data;
        }
    }

    public void handleEntityInteraction(Player player, Entity entity, EquipmentSlot hand) {
        var item = player.getInventory().getItem(hand);

        if (levelCandyItem != null && Registries.ITEM.compare(levelCandyItem, item)) {
            if (!isMonsterMon(entity))
                return;

            var monsterData = getMonsterData(entity);
            if (monsterData == null)
                return;
            var level = monsterData.level();

            /// Check if it is owned by the player first
            if (monsterData.isOwner(player)) {
                player.sendMessage(getMessage(player, "item_level_candy_not_owned"));
                return;
            }

            if (level >= MAX_LEVEL) {
                player.sendMessage(getMessage(player, "item_level_candy_max_level"));
                return;
            }

            item.setAmount(item.getAmount() - 1);

            monsterData.level(level + 1);

            setMonsterData(entity, monsterData);

            player.playSound(entity.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            player.sendMessage(getMessage(player, "item_level_candy_levelup"));
        }
    }

    private String getMessage(Player player, String messageKey) {
        var msg = "";
        var event = new MonsterMonsMessageEvent(msg, messageKey, player);
        Bukkit.getPluginManager().callEvent(event);
        return event.getMessage();
    }

    private void setMonsterData(Entity entity, MonsterData monsterData) {
        var config = new PersistentDataContainerConfig(entity.getPersistentDataContainer(), null);
        config.set("monsterdata", monsterData);
    }

    public void handleInteraction(PlayerInteractEvent e) {
        var action = e.getAction();
        var hand = e.getHand();
        if (hand == null)
            return;
        var player = e.getPlayer();
        var item = player.getInventory().getItem(hand);

        if (!action.equals(Action.RIGHT_CLICK_BLOCK))
            return;
        if (!item.hasItemMeta())
            return;
        var meta = item.getItemMeta();

        var config = new PersistentDataContainerConfig(meta.getPersistentDataContainer(), (container) -> item.setItemMeta(meta));

        var ballData = config.get("balldata", MonsterBallData.class);
        if (ballData == null)
            return;
        var monsterData = ballData.monsterData();
        if (monsterData == null) {
            return;
        }
        /// remove the item before adding to the map
        item.setAmount(item.getAmount() - 1);

        var interactionPoint = e.getInteractionPoint();
        var world = e.getPlayer().getWorld();

        var entity = world.spawnEntity(interactionPoint, monsterData.base());
        setMonsterData(entity, monsterData);
        updateEntityName(entity);

        world.dropItem(interactionPoint,  Registries.ITEM.createItemStack(ballData.getBall()));
    }

    @SuppressWarnings("UnstableApiUsage")
    private void handleMonsterBallEnter(BallThrow throwData, Entity entity, MonsterData monsterData) {
        var loc = entity.getLocation();

        /// Remove entity after snapshot created
        entity.remove();

        var captured = getCapturedItem(throwData, monsterData);

        loc.getWorld().dropItem(loc, captured);
    }

    /// [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Catch_rate)
    /// We are using the sword and shield generation for reference for the catch formula
    /**
     *
     * @return a value from 0.0 - 1.0
     */
    private double calculateCatchProbability(@Range(from = 0, to = 100) double ballCapturePower, double monLevel, double currentHealth, double maxHealth) {
        if (ballCapturePower >= 100.0)
            return 1.0;

        /// We are using the catch rate of caterpie as the default
        /// [キャタピー]https://bulbapedia.bulbagarden.net/wiki/Caterpie_(Pok%C3%A9mon)
        /// catch rate is between 1 and 255
        final int defaultMonCatchRate = 255;
        int catchRate = defaultMonCatchRate;

        double healthFactor = (3.0 * maxHealth - 2.0 * currentHealth) / (3.0 * maxHealth);
        double rateModified = Math.max(1.0, Math.min(255.0, catchRate));
        double levelBonus = Math.max(1.0, (30.0 - monLevel) / 10.0);

        double ballBonus = 1.0 + (ballCapturePower / 100.0);

        double statusBonus = 1.0;
        double difficulty = 1.0;

        double a = Math.floor(healthFactor * 4096.0 * rateModified * ballBonus);

        a *= levelBonus;
        a *= statusBonus;
        a *= difficulty;

        return Math.min(
                1.0,
                Math.pow(a / 1044480.0, 0.75)
        );
    }

    /// used for when the ball needs to be drops at where it hit
    /// also used for spawning the ball to reclaim it
    private void spawnBallOnProjectile(BallThrow throwData, Projectile projectile) {
        var jitem = Registries.ITEM.getById(throwData.ball().getId());
        projectile.getWorld().dropItem(projectile.getLocation(), Registries.ITEM.createItemStack(jitem));
    }

    private void notifyPlayerOfInvalidCatch(Player player) {
        player.sendMessage(getMessage(player, "uncatchable_creature"));
    }

    /**
     *
     * @throws IllegalArgumentException if it cannot retrieve material
     */
    private Material getSpawnEggMaterial(EntityType type) {
        return Material.valueOf(type.name() + "_SPAWN_EGG");
    }

    //TODO there is a really wierd back the the item that you used to throw the ball does not have metadata
    private ItemStack getCapturedItem(BallThrow throwData, MonsterData monsterData) {
        var spawnEgg = getSpawnEggMaterial(monsterData.base());
        var date = monsterData.dateCaught();
        var level = monsterData.level();

        var captured = new ItemStack(spawnEgg);
        var meta = captured.getItemMeta();

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
        var stack = Registries.ITEM.createItemStack(throwData.ball);
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

        var config = new PersistentDataContainerConfig(meta.getPersistentDataContainer(), (container) -> stack.setItemMeta(meta));
        config.set("balldata", new MonsterBallData(throwData.ball().getId(), monsterData));

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
