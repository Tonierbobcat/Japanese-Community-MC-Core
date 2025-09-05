package com.loficostudios.japaneseMinecraft;

/// Feel free to translate these docs to Japanese for yourself in order to understand them better

import com.loficostudios.forgified.paper.IPluginResources;
import com.loficostudios.forgified.paper.ItemRegistry;
import com.loficostudios.forgified.paper.JItem;

/**
 * The idea is that this class is the definition of all the custom items and custom implementation can be added through listeners
 * You can use JItem::isItem(ItemStack) to check if an item is a specific custom item
 */
public class Items {

    public static final ItemRegistry ITEMS = new ItemRegistry();

    /// DEFINE ITEMS
    ///
    public static final JItem FLOWER_SWORD = ITEMS.create("flower_sword");

    public static final JItem GIFT_BAG = ITEMS.create("gift_bag");

    /// Called JavaPlugin.onEnable()
    public static void register(IPluginResources resources) {
        ITEMS.initialize(resources);
    }
}
