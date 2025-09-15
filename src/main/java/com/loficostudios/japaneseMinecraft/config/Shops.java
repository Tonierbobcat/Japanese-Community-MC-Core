package com.loficostudios.japaneseMinecraft.config;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.commands.VanillaShopItem;
import com.loficostudios.japaneseMinecraft.shop.Shop;
import com.loficostudios.japaneseMinecraft.shop.ShopInstance;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Shops {
    public static final Shop<VanillaShopItem> BUILDER_SHOP;
    static {
        Map<Material, Integer> items = new LinkedHashMap<>();

        items.put(Material.OAK_LOG, 5);
        items.put(Material.SPRUCE_LOG, 5);
        items.put(Material.BIRCH_LOG, 5);
        items.put(Material.JUNGLE_LOG, 6);
        items.put(Material.ACACIA_LOG, 6);
        items.put(Material.DARK_OAK_LOG, 7);
        items.put(Material.MANGROVE_LOG, 6);
        items.put(Material.CHERRY_LOG, 7);
        items.put(Material.PALE_OAK_LOG, 7);
        items.put(Material.BAMBOO_BLOCK, 3);

        items.put(Material.STONE, 3);
        items.put(Material.COBBLESTONE, 2);
        items.put(Material.MOSSY_COBBLESTONE, 3);
        items.put(Material.STONE_BRICKS, 4);
        items.put(Material.GRANITE, 3);
        items.put(Material.DIORITE, 3);
        items.put(Material.ANDESITE, 3);
        items.put(Material.DEEPSLATE, 5);
        items.put(Material.COBBLED_DEEPSLATE, 5);
        items.put(Material.TUFF, 2);

        items.put(Material.BRICKS, 5);
        items.put(Material.MUD, 2);
        items.put(Material.MUD_BRICKS, 3);
        items.put(Material.SANDSTONE, 4);
        items.put(Material.RED_SANDSTONE, 4);
        items.put(Material.NETHER_BRICKS, 6);
        items.put(Material.END_STONE, 8);
        items.put(Material.PURPUR_BLOCK, 7);
        items.put(Material.BLACKSTONE, 5);
        items.put(Material.POLISHED_BLACKSTONE_BRICKS, 6);
        items.put(Material.QUARTZ_BLOCK, 10);
        items.put(Material.GLASS, 2);

        items.put(Material.GRASS_BLOCK, 1);
        items.put(Material.DIRT, 1);
        items.put(Material.ICE, 4);
        items.put(Material.PACKED_ICE, 5);
        items.put(Material.SAND, 1);
        items.put(Material.GRAVEL, 1);
        items.put(Material.TERRACOTTA, 1);

        List<String> colors = new ArrayList<>();
        colors.add("WHITE");
        colors.add("LIGHT_GRAY");
        colors.add("GRAY");
        colors.add("BLACK");
        colors.add("BROWN");
        colors.add("RED");
        colors.add("ORANGE");
        colors.add("LIME");
        colors.add("GREEN");
        colors.add("CYAN");
        colors.add("LIGHT_BLUE");
        colors.add("BLUE");
        colors.add("PURPLE");
        colors.add("MAGENTA");
        colors.add("PINK");

        Map<String, Integer> prices = Map.of(
                "WOOL", 2,
                "TERRACOTTA", 3,
                "STAINED_GLASS", 3,
                "CONCRETE", 4,
                "CONCRETE_POWDER", 3
        );

        for (String color : colors) {
            items.put(Material.valueOf(color + "_WOOL"), prices.get("WOOL"));
            items.put(Material.valueOf(color + "_TERRACOTTA"), prices.get("TERRACOTTA"));
            items.put(Material.valueOf(color + "_STAINED_GLASS"), prices.get("STAINED_GLASS"));
            items.put(Material.valueOf(color + "_CONCRETE"), prices.get("CONCRETE"));
            items.put(Material.valueOf(color + "_CONCRETE_POWDER"), prices.get("CONCRETE_POWDER"));
        }

        var instances = items.entrySet().stream()
                .map(a -> new ShopInstance<>(false, a.getValue(), new VanillaShopItem(a.getKey())))
                .toList();

        BUILDER_SHOP = Shop.create(instances, JapaneseMinecraft.getEconomyProvider());
    }
}
