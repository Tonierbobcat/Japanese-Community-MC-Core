package com.loficostudios.japaneseMinecraft;


import com.loficostudios.forgified.paper.items.BowItem;
import com.loficostudios.forgified.paper.items.ItemRegistry;
import com.loficostudios.forgified.paper.items.JItem;
import com.loficostudios.forgified.paper.items.SwordItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

@SuppressWarnings("UnstableApiUsage")
public class Items {
    public static final ItemRegistry ITEMS = new ItemRegistry();

    public static final JItem FLOWER_SWORD = ITEMS.create("flower_sword",
            () -> new SwordItem(Material.WOODEN_SWORD, 5, 1.8, JItem.Properties.empty()));

    public static final JItem FLOWER_BOW = ITEMS.create("flower_bow", FlowerBow::new);

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
}
