package com.loficostudios.japaneseMinecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/// TODO THIS NEEDS TO BE WORKED ON. INCLUDED IN CLASS ITEM CREATION UTILS
public class Items {
    public static final ItemStack FLOWER_SWORD;

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
