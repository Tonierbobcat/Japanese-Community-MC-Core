package com.loficostudios.japaneseMinecraft;


import com.loficostudios.forgified.paper.items.BowItem;
import com.loficostudios.forgified.paper.items.ItemRegistry;
import com.loficostudios.forgified.paper.items.JItem;
import com.loficostudios.forgified.paper.items.SwordItem;
import com.loficostudios.forgified.paper.items.armor.ArmorItem;
import com.loficostudios.forgified.paper.items.armor.ArmorMaterial;
import com.loficostudios.japaneseMinecraft.pokemon.MonsterBall;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Please put documentation on your item do tell other developers what is used for

@SuppressWarnings("UnstableApiUsage")
public class Items {

    /// We need to override the item name because geyser cannot handle translatable item components
    public static final ItemRegistry ITEMS = new ItemRegistry(List.of(
            (item, stack) -> {
                var meta = stack.getItemMeta();

                meta.displayName(Component.text(Common.formatEnumName(item.getId()))
                        .decoration(TextDecoration.ITALIC, false));

                stack.setItemMeta(meta);
            }));

    public static final JItem FLOWER_SWORD = ITEMS.create("flower_sword",
            () -> new SwordItem(Material.WOODEN_SWORD, 5, 1.8, new JItem.Properties()
                    .custom((item) -> {
                        item.setData(DataComponentTypes.BLOCKS_ATTACKS, BlocksAttacks.blocksAttacks()
                                .blockSound(Sound.ENCHANT_THORNS_HIT.getKey()).build());
                    })));

    public static final JItem FLOWER_BOW = ITEMS.create("flower_bow", FlowerBow::new);

    public static final JItem FLOWER_HELMET = ITEMS.create("flower_helmet",
            () -> new ArmorItem(EquipmentSlot.HEAD, ArmorMaterials.FLOWER));

    public static final JItem KEBAB = ITEMS.create("kebab", () -> new JItem(Material.COOKED_BEEF, new JItem.Properties()
            .food(FoodProperties.food()
                    .nutrition(10)
                    .saturation(10)
                    .build())
            .model()));

    public static final JItem RAW_CRAB = ITEMS.create("raw_crab", () -> new JItem(Material.COD, new JItem.Properties()
            .food(FoodProperties.food()
                    .nutrition(1)
                    .saturation(1)
                    .build())
            .model()));

    public static final JItem BUILDERS_CHEST = ITEMS.create("builders_chest", () -> new JItem(Material.CHEST, JItem.Properties.empty()));

    public static final JItem EDIBLE_IRON_PICKAXE = ITEMS.create("edible_iron_pickaxe", () -> new JItem(Material.IRON_PICKAXE, new JItem.Properties()
            .food(FoodProperties.food().nutrition(20).saturation(20).build())
            .custom((item) -> {
                var effects = List.of(new PotionEffect(PotionEffectType.HASTE, 60, 255));
                item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable().addEffect(ConsumeEffect.applyStatusEffects(effects, 1)).build());
            })
            .model()));

    /// Used to level up monstermons
    public static final JItem LEVEL_CANDY = ITEMS.create("level_candy", () -> new JItem(Material.EMERALD, JItem.Properties.empty()));

    public static final JItem BASIC_FLASHLIGHT = ITEMS.create("basic_flashlight", () -> new JItem(Material.TORCH, new JItem.Properties()));

    public static final JItem REGION_STICK = ITEMS.create("region_stick", () -> new JItem(Material.STICK, new JItem.Properties()
            .model()));

    public static final JItem MASTER_BALL = ITEMS.create("master_ball", () -> new MonsterBall(10, new JItem.Properties()));
    public static final JItem TELEPORT_CRYSTAL = ITEMS.create("teleport_crystal", () -> new JItem(Material.AMETHYST_SHARD, JItem.Properties.empty()));

    public static final JItem POKEBALL = ITEMS.create("pokeball", () -> new MonsterBall(1, new JItem.Properties()));

    public static <T> @Nullable T getItemFromItem(ItemStack stack, Class<T> clazz) {
        var id = getId(stack);
        if (id == null)
            return null;
        var item = Items.ITEMS.getById(id);
        return clazz.isInstance(item) ? clazz.cast(item) : null;
    }

    public static boolean isItem(ItemStack stack, JItem item) {
        if (stack == null)
            return false;
        var id = getId(stack);
        if (id == null)
            return false;
        return id.equals(item.getId());
    }

    private static @Nullable String getId(ItemStack stack) {
        if (stack.getType().equals(Material.AIR) || !stack.hasItemMeta())
            return null;
        var pdc = stack.getItemMeta().getPersistentDataContainer();
        var id = pdc.get(Items.ITEMS.getItemKey(), PersistentDataType.STRING);
        return id;
    }

    /// DEBUG
    public static class FlowerBow extends BowItem {
        public FlowerBow() {
            super(JItem.Properties.empty());
        }

        @Override
        public void onShoot(EntityShootBowEvent e) {
            Common.notifyPlayers("Flower Bow Projectile shot!!!");
        }

        @Override
        public void onHit(ProjectileHitEvent e) {
            Common.notifyPlayers("Flower Bow Projectile hit!!!");
        }
    }

    /// DEBUG
    public enum ArmorMaterials implements ArmorMaterial {
        FLOWER(7,  new int[]{5, 2, 7, 2}, 10f, 0.0f);

        private static final int[] DURABILITY_PER_SLOT = new int[]{13, 15, 16, 11};

        private final int durabilityMultiplier;
        private final int[] slotProtections;
        private final float toughness;
        private final float knockbackResistance;

        ArmorMaterials(int durabilityMultiplier, int[] slotProtections, float toughness, float knockbackResistance) {
            this.durabilityMultiplier = durabilityMultiplier;
            this.slotProtections = slotProtections;
            this.toughness = toughness;
            this.knockbackResistance = knockbackResistance;
        }

        @Override
        public int getDurabilityForSlot(EquipmentSlot slot) {
            return DURABILITY_PER_SLOT[getSlotIndex(slot)] * this.durabilityMultiplier;
        }

        @Override
        public int getDefenseForSlot(EquipmentSlot slot) {
            return this.slotProtections[getSlotIndex(slot)];
        }

        @Override
        public float getToughness() {
            return toughness;
        }

        @Override
        public float getKnockbackResistance() {
            return knockbackResistance;
        }

        private int getSlotIndex(EquipmentSlot slot) {
            int index;
            switch (slot) {
                case HEAD -> index = 3;
                case CHEST -> index = 2;
                case LEGS -> index = 1;
                case FEET -> index = 0;
                default -> throw new IllegalArgumentException("Invalid EquipmentSlot");
            }
            return index;
        }
    }
}
