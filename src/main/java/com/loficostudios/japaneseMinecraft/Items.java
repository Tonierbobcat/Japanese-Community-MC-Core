package com.loficostudios.japaneseMinecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

/// TODO THIS NEEDS TO BE WORKED ON. INCLUDED IN CLASS ITEM CREATION UTILS
public class Items {
    public static final ItemStack FLOWER_SWORD;

    public static ItemStack createGiftBag(Player sender, Player receiver, ItemStack... items) {
        var item = new ItemStack(Material.BUNDLE);
        var meta = ((BundleMeta) item.getItemMeta());
        if (meta != null) {
            for (int i = 0; i < 10; i++) {
                meta.addItem(ItemStack.of(Material.DIRT, 64));
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    static {
        var item = new ItemStack(Material.IRON_SWORD);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("花の剣").decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
        FLOWER_SWORD = item;
    }
}
