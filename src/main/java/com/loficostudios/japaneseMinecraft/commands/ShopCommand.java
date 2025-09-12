package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;
import com.loficostudios.japaneseMinecraft.shop.*;
import com.loficostudios.japaneseMinecraft.shop.gui.GuiIcon;
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public class ShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("Only players can use this!!");
            return true;
        }

        List<ShopInstance<VanillaShopItem>> items = new LinkedList<>();
        for (Material material : Material.values()) {
            /// validate
            try {
                new ItemStack(material);
            } catch (Exception e) {
                continue;
            }
            items.add(new ShopInstance<>(1, new VanillaShopItem(material)));
        }


        var shop = Shop.create(items, new EconomyProvider() {
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


        new ShopGui<>(shop, getGenericShopTemplate(Component.text("Shop")), JapaneseMinecraft::getPlayerProfile)
                .open(sender);
        return true;
    }

    public static <T extends ShopItem<T>> ShopGuiTemplate<T> getGenericShopTemplate(Component title) {
        return new ShopGuiTemplate.Builder<T>(5 * 9, (shop) -> title)
                .onBuy(ShopCommand::onBuy)
                .onInstance(((instance, player) -> {
                }))
                .setSelectionScreenSize(9)
                .setSelectionScreenTitle((instance) -> Component.text("Selection Screen"))
                .setAddAmountButton((instance, amount) -> Component.text("§a+" + amount), (instance, amount) -> List.of())
                .setRemoveAmountButton((instance, amount) -> Component.text("§c-" + amount), (instance, amount) -> List.of())
                .icon((instance, icon, amount) -> {
                    var item = icon.item();

                    var description = icon.description();
                    if (!description.isEmpty())
                        description.add(getPriceMessage(instance, icon, amount));
                    else description = Collections.singletonList(getPriceMessage(instance, icon, amount));

                    icon.description(description);
                })
                .build();
    }

    public static <T extends ShopItem<T>> Component getPriceMessage(ShopInstance<T> instance, GuiIcon icon, int amount) {
        if (icon == null)
            return Component.text("NaN");


        var cost = instance.getCost();
        var total = cost * amount;
        var base = instance.getBaseCost();
        var original = base * amount;

        var hasModifiers = !instance.getModifiers().isEmpty() && cost > base || cost < base;

        var line = (String) null;
        if (instance.getItem() instanceof Stackable && amount > 1) {
            if (hasModifiers) {
                line = "§fʀɪᴄᴇ: §e§m${original}§r §e${total} §8§m${cost}§r §8${base} ᴇᴀ"
                        .replace("{original}", String.format("%.3f", original)).replace("{base}", String.format("%.3f",base))
                        .replace("{total}", String.format("%.3f", total).replace("{cost}", String.format("%.3f",cost)));
            } else {
                line = "§fʀɪᴄᴇ: §e${total} §8${cost} ᴇᴀ"
                        .replace("{total}", String.format("%.3f", total)).replace("{cost}", String.format("%.3f",cost));
            }
        } else {
            if (hasModifiers) {
                line = "§fᴘʀɪᴄᴇ: §e§m${original}§r §e${total}"
                        .replace("{original}", String.format("%.3f", original))
                        .replace("{total}", String.format("%.3f", total));
            } else {
                line = "§fᴘʀɪᴄᴇ: §e${total}"
                        .replace("{total}", String.format("%.3f", total));
            }
        }
        return Component.text(line);
    }

    public static final String SHOP_SUCCESSFULLY_PURCHASED_ITEM = "You successfully purchased {amount}x {item}";

    public static final String SHOP_NOT_ENOUGH_MONEY = "You do not have enough money to buy {amount}x {item}!";

    public static final String SHOP_NOT_ENOUGH_INVENTORY_SPACE = "<red>Not enough inventory space!";

    public static <T extends ShopItem<T>> void onBuy(ShopTransactionResult<T> result, Player player) {
        switch (result.type()) {
            case NO_INVENTORY_SPACE -> player.sendMessage(SHOP_NOT_ENOUGH_INVENTORY_SPACE);
            case SUCCESS -> player.sendMessage(SHOP_SUCCESSFULLY_PURCHASED_ITEM.replace("{amount}", "" + result.amount())
                    .replace("{item}", result.instance().getItem().getName()));
            case NOT_ENOUGH_MONEY -> player.sendMessage(SHOP_NOT_ENOUGH_MONEY.replace("{amount}", "" +  result.amount())
                    .replace("{item}", result.instance().getItem().getName()));
            default -> throw new IllegalArgumentException("Unhandled TransactionResult: " + result.type());
        }
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
