package com.loficostudios.japaneseMinecraft;

/// Feel free to translate these docs to Japanese for yourself in order to understand them better

import com.loficostudios.forgified.paper.items.ItemRegistry;
import com.loficostudios.forgified.paper.items.JItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * The idea is that this class is the definition of all the custom items and custom implementation can be added through listeners
 * You can use JItem::isItem(ItemStack) to check if an item is a specific custom item
 */
public class Items {

    public static final ItemRegistry ITEMS = new ItemRegistry();

    public static final JItem FLOWER_SWORD = ITEMS.create("flower_sword", new DummyItem());

    public static final JItem GIFT_BAG = ITEMS.create("gift_bag", new DummyItem());

//    /// FARMING
//    public static final JItem RICE = ITEMS.create("rice");
//    public static final JItem CUCUMBER = ITEMS.create("cucumber");
//
//    /// SUSHI INGREDIENTS
//    public static final JItem COOKED_RICE = ITEMS.create("cooked_rice");
//
//    /// Recipe: Cucumber + Cooked Rice + Dried Kelp (minecraft) + Crab Meat
//    public static final JItem CALIFORNIA_ROLL = ITEMS.create("california_roll");
//
//    public static final JItem CRAB_MEAT = ITEMS.create("crab_meat");
//    public static final JItem COOKED_CRAB_MEAT = ITEMS.create("cooked_crab_meat");
//
//    public static final JItem TUNA_ONIGIRI = ITEMS.create("tuna_onigiri");
//
//    public static final JItem SALMON_NIGIRI = ITEMS.create("salmon_nigiri");
//    public static final JItem TUNA_NIGIRI = ITEMS.create("tuna_nigiri");
//    public static final JItem EEL_NIGIRI = ITEMS.create("eel_nigiri");
//    public static final JItem SHRIMP_NIGIRI = ITEMS.create("shrimp_nigiri");
//
//    /// FISH
//    public static final JItem RAW_OCTOPUS = ITEMS.create("raw_octopus");
//    public static final JItem TAKO_YAKI = ITEMS.create("tako_yaki");
//    public static final JItem COOKED_OCTOPUS = ITEMS.create("cooked_octopus");
//
//    public static final JItem RAW_SQUID = ITEMS.create("raw_squid");
//    public static final JItem COOKED_SQUID = ITEMS.create("cooked_squid");
//
//    public static final JItem EEL = ITEMS.create("raw_eel");
//    public static final JItem COOKED_EEL = ITEMS.create("cooked_eel");
//
//    /// maguro is the japanese word for tuna
//    public static final JItem MAGURO = ITEMS.create("raw_maguro");
//    public static final JItem COOKED_MAGURO = ITEMS.create("cooked_maguro");
//
//    /// ALCOHOL
//    public static final JItem SAKE = ITEMS.create("sake");

    /// METHOD to compare an ItemStack to a JItem should be in the forgified library
    public static boolean compare(JItem item, ItemStack stack) {
        var meta = stack.getItemMeta();
        if (meta == null) return false;
        var pdc = meta.getPersistentDataContainer();
        if (!pdc.has(ITEMS.getItemKey(), PersistentDataType.STRING))
            return false;
        var id = pdc.get(ITEMS.getItemKey(), PersistentDataType.STRING);
        return item.getId().equals(id);
    }
}
