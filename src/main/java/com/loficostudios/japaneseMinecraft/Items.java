package com.loficostudios.japaneseMinecraft;

import io.papermc.paper.datacomponent.item.CustomModelData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Consumer;

/// TODO THIS NEEDS TO BE WORKED ON. INCLUDED IN CLASS ITEM CREATION UTILS
/// ITEM NAMES WILL REMAIN IN ENGLISH FOR SIMPLICITY

/**
 * The idea is that this class is the definition of all the custom items and custom implementation can be added through listeners
 * You can use Items::TestItem(itemInHand, target) to check if an item is a specific custom item
 */
@SuppressWarnings("UnstableApiUsage")
public class Items {
    /// DEFINE MODEL IDS FOR RESOURCE PACK DEVELOPERS
    private static final int GIFT_BAG_MODEL_ID = 19283;

    /// DEFINE ITEMS
    public static final ItemStack FLOWER_SWORD;
    /// GIFT BAG STUB
    public static final ItemStack GIFT_BAG = createGiftBag(null, null);

    /**
     *
     * @throws IllegalArgumentException if more than 12 different item types are included
     */
    public static ItemStack createGiftBag(Player sender, Player receiver, ItemStack... items) {
        if (items.length > 12) {
            throw new IllegalArgumentException("A gifts can only contain up to 12 different item types.");
        }

        var id = "gift_bag";
        var name = "§dGift Bag";
        var description = List.of("§aA gift from " + (sender != null ? sender.getName() : "null"));

        return createItem(id, name, description, Material.BUNDLE, meta -> {
            /// Surrounding with try-catch for future proofing
            for (ItemFlag flag : ItemFlag.values()) {
                try {
                    meta.addItemFlags(flag);
                } catch (Exception ignore) {
                }
            }

            meta.getCustomModelDataComponent()
                    .setFloats(List.of((float) GIFT_BAG_MODEL_ID));
            for (ItemStack i : items) {
                ((BundleMeta) meta).addItem(i);
            }
        });
    }

    /// For internal use only
    @ApiStatus.Internal
    private static ItemStack createItem(String id, String name, List<? extends String> description, Material material, Consumer<ItemMeta> meta) {
        var item = new ItemStack(material);
        var itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(JapaneseMinecraft.getNMK("items"), PersistentDataType.STRING, id);
            itemMeta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
            itemMeta.lore(description.stream().map(s -> Component.text(s).decoration(TextDecoration.ITALIC, false)).toList());
            meta.accept(itemMeta);
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    /// I prefer you create a custom class for each item with a MyCustomItem::Build() method to keep things organized
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
