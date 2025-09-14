package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.forgified.paper.gui.GuiIcon;
import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;
import com.loficostudios.japaneseMinecraft.shop.*;
import com.loficostudios.japaneseMinecraft.shop.gui.ShopGui;
import com.loficostudios.japaneseMinecraft.shop.gui.ShopGuiIcon;
import com.loficostudios.japaneseMinecraft.shop.gui.ShopGuiTemplate;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

public class ShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("Only players can use this!!");
            return true;
        }

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

        var shop = Shop.create(items.entrySet().stream().map(a -> new ShopInstance<>(a.getValue(), new VanillaShopItem(a.getKey()))).toList(), new EconomyProvider() {
            @Override
            public boolean has(PlayerProfile player, double amount) {
                return player.hasMoney(((int) Math.round(amount)));
            }

            @Override
            public boolean withdrawalPlayer(PlayerProfile player, double amount) {
                player.subtractMoney(((int) Math.round(amount)));
                return true;
            }

            @Override
            public boolean depositPlayer(PlayerProfile player, double amount) {
                player.addMoney(((int) Math.round(amount)));
                return true;
            }
        });

        new ShopGui<>(shop, ShopGuiTemplate.generic(Component.text("Shop")), JapaneseMinecraft::getPlayerProfile)
                .open(sender);
        return true;
    }

    private static class VanillaShopItem implements ShopItem<VanillaShopItem>, Stackable, ShopGuiIcon<VanillaShopItem> {
        private final Material material;

        private VanillaShopItem(Material material) {
            this.material = material;
        }

        @Override
        public ShopTransactionResult<VanillaShopItem> onBuy(ShopInstance<VanillaShopItem> instance, int amount, PlayerProfile profile) {
            var player = profile.getPlayer();
            if (player == null) {
                Debug.logError("player is not online!!");
                return new ShopTransactionResult<>(instance, amount, ShopTransactionResult.Type.FAILURE);
            }
            var stack = new ItemStack(material, Math.max(amount, 1));
            player.getInventory().addItem(stack);
            return new ShopTransactionResult<>(instance, amount, ShopTransactionResult.Type.SUCCESS);
        }

        @Override
        public String getName() {
            return Common.formatEnumName(material);
        }

        @Override
        public GuiIcon getIcon(ShopInstance<VanillaShopItem> instance, int amount, BiConsumer<Player, ClickType> onClick) {
            var icon = new GuiIcon(instance.getItem().material, Component.text(instance.getItem().getName()));
            icon.onClick(onClick);
            return icon;
        }
    }
}
