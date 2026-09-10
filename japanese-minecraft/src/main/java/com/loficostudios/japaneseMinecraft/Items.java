package com.loficostudios.japaneseMinecraft;


import com.loficostudios.forgified.paper.items.BowItem;
import com.loficostudios.forgified.paper.items.JItem;
import com.loficostudios.forgified.paper.items.SwordItem;
import com.loficostudios.forgified.paper.items.armor.ArmorItem;
import com.loficostudios.forgified.paper.items.armor.ArmorMaterial;
import com.loficostudios.forgified.registry.DeferredRegistry;
import com.loficostudios.forgified.registry.Registries;
import com.loficostudios.forgified.registry.RegistryObject;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/// Please put documentation on your item do tell other developers what is used for

@SuppressWarnings("UnstableApiUsage")
public class Items {

    /// We need to override the item name because geyser cannot handle translatable item components
    public static final DeferredRegistry<JItem> ITEMS = Registries.create(JItem.class, JapaneseMinecraft.NAMESPACE);

    public static final RegistryObject<JItem> FLOWER_SWORD = ITEMS.register("flower_sword",
            () -> new SwordItem(Material.WOODEN_SWORD, 5, 1.8, JItem.Properties.empty()
                    .custom((item) -> {
                        item.setData(DataComponentTypes.BLOCKS_ATTACKS, BlocksAttacks.blocksAttacks()
                                .blockSound(Sound.ENCHANT_THORNS_HIT.getKey()).build());
                    })));

    public static final RegistryObject<JItem> FLOWER_BOW = ITEMS.register("flower_bow", FlowerBow::new);

    public static final RegistryObject<JItem> FLOWER_HELMET = ITEMS.register("flower_helmet",
            () -> new ArmorItem(EquipmentSlot.HEAD, ArmorMaterials.FLOWER));

    public static final RegistryObject<JItem> KEBAB = ITEMS.register("kebab", () -> new JItem(Material.COOKED_BEEF, JItem.Properties.empty()
            .consumable(new com.loficostudios.forgified.paper.items.properties.Consumable(10,10))));

    public static final RegistryObject<JItem> RAW_CRAB = ITEMS.register("raw_crab", () -> new JItem(Material.COD, JItem.Properties.empty()
            .consumable(new com.loficostudios.forgified.paper.items.properties.Consumable(1,1))));

    public static final RegistryObject<JItem> BUILDERS_CHEST = ITEMS.register("builders_chest", () -> new JItem(Material.CHEST, JItem.Properties.empty()));

    public static final RegistryObject<JItem> EDIBLE_IRON_PICKAXE = ITEMS.register("edible_iron_pickaxe", () -> new JItem(Material.IRON_PICKAXE, JItem.Properties.empty()
            .consumable(new com.loficostudios.forgified.paper.items.properties.Consumable(20, 20))
            .custom((item) -> {
                var effects = List.of(new PotionEffect(PotionEffectType.HASTE, 60, 255));
                item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable().addEffect(ConsumeEffect.applyStatusEffects(effects, 1)).build());
            })));

    public static final RegistryObject<JItem> BASIC_FLASHLIGHT = ITEMS.register("basic_flashlight", () -> new JItem(Material.TORCH, JItem.Properties.empty()));

    public static final RegistryObject<JItem> REGION_STICK = ITEMS.register("region_stick", () -> new JItem(Material.STICK, JItem.Properties.empty()));

    public static final RegistryObject<JItem> TELEPORT_CRYSTAL = ITEMS.register("teleport_crystal", () -> new JItem(Material.AMETHYST_SHARD, JItem.Properties.empty()));


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
