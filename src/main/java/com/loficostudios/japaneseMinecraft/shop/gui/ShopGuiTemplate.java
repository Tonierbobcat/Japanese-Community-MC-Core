package com.loficostudios.japaneseMinecraft.shop.gui;

import com.loficostudios.forgified.paper.gui.GuiIcon;
import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.shop.*;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ShopGuiTemplate<Impl extends ShopItem<Impl>> {

    private final int size; // todo autosize
    private final int selectionSize; // todo autosize

    public int size() {
        return size;
    }

    public int selectionSize() {
        return selectionSize;
    }

    private final Function<Shop<Impl>, Component> title;

    //        private final BiFunction<ShopInstance<Impl>, Integer, List<Component>> price;
    public TriConsumer<ShopInstance<Impl>, GuiIcon, Integer> handleIcon;

    private BiConsumer<ShopInstance<Impl>, Player> handleInstance;

    private final Function<ShopInstance<Impl>, Component> selectionTitle;

    private final BiFunction<ShopInstance<Impl>, Integer, Component> removeButtonName;
    private final BiFunction<ShopInstance<Impl>, Integer, List<Component>> removeButtonDescription;
    private final BiFunction<ShopInstance<Impl>, Integer, Component> addButtonName;
    private final BiFunction<ShopInstance<Impl>, Integer, List<Component>> addButtonDescription;
    private final BiConsumer<ShopTransactionResult<Impl>, Player> onBuy;

    private ShopGuiTemplate(Builder<Impl> builder) {
        this.size = builder.size;
        this.selectionTitle = builder.selectionTitle;
        this.selectionSize = builder.selectionSize;
        this.title = builder.title;
//            this.price = builder.price;
        this.handleIcon = builder.handleIcon;
        this.handleInstance = builder.handleInstance;
        this.removeButtonName = builder.removeButtonName;
        this.removeButtonDescription = builder.removeButtonDescription;
        this.addButtonName = builder.addButtonName;
        this.addButtonDescription = builder.addButtonDescription;
        this.onBuy = builder.onBuy;
    }

    public ShopGuiTemplate<Impl> icon(TriConsumer<ShopInstance<Impl>, GuiIcon, Integer> icon) {
        this.handleIcon = icon;
        return this;
    }

    public void onBuy(ShopTransactionResult<Impl> result, Player player) {
        try {
            this.onBuy.accept(result, player);
        } catch (Exception e) {
            Debug.logError("ShopGuiTemplate#onBuy. " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ShopGuiTemplate<Impl> onInstance(BiConsumer<ShopInstance<Impl>, Player> onInstance) {
        this.handleInstance = onInstance;
        return this;
    }

    public BiConsumer<ShopInstance<Impl>, Player> onInstance() {
        return this.handleInstance;
    }

    public Component title(Shop<Impl> shop) {
        return title.apply(shop);
    }

    public Component selectionTitle(ShopInstance<Impl> instance) {
        return selectionTitle.apply(instance);
    }

    public ItemStack getRemoveAmountIcon(ShopInstance<Impl> instance, int amount) {
        var item = ItemStack.of(Material.RED_STAINED_GLASS_PANE);
        var meta = Objects.requireNonNull(item.getItemMeta());

        meta.displayName(removeButtonName.apply(instance, amount));

        var description = removeButtonDescription.apply(instance, amount);
        if (!description.isEmpty()) {
            meta.lore(description);
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getAddAmountIcon(ShopInstance<Impl> instance, int amount) {
        var item = ItemStack.of(Material.LIME_STAINED_GLASS_PANE);
        var meta = Objects.requireNonNull(item.getItemMeta());

        meta.displayName(addButtonName.apply(instance, amount));

        var description = addButtonDescription.apply(instance, amount);
        if (!description.isEmpty()) {
            meta.lore(description);
        }

        item.setItemMeta(meta);
        return item;
    }

    public static class Builder<Impl extends ShopItem<Impl>> {
        private final Integer size;
        private TriConsumer<ShopInstance<Impl>, GuiIcon, Integer> handleIcon;
        private Integer selectionSize;

        private final Function<Shop<Impl>, Component> title;

        private BiFunction<ShopInstance<Impl>, Integer, List<Component>> price;

        private BiConsumer<ShopInstance<Impl>, Player> handleInstance;

        private Function<ShopInstance<Impl>, Component> selectionTitle;


        private BiFunction<ShopInstance<Impl>, Integer, Component> removeButtonName;
        private BiFunction<ShopInstance<Impl>, Integer, List<Component>> removeButtonDescription;
        private BiFunction<ShopInstance<Impl>, Integer, Component> addButtonName;
        private BiFunction<ShopInstance<Impl>, Integer, List<Component>> addButtonDescription;

        private BiConsumer<ShopTransactionResult<Impl>, Player> onBuy;

        public Builder(int size, Function<Shop<Impl>, Component> title) {
            this.size = size;
            this.title = title;
        }

        public Builder<Impl> icon(TriConsumer<ShopInstance<Impl>, GuiIcon, Integer> icon) {
            this.handleIcon = icon;
            return this;
        }

        public Builder<Impl> onBuy(BiConsumer<ShopTransactionResult<Impl>, Player> onBuy) {
            this.onBuy = onBuy;
            return this;
        }
        public Builder<Impl> onInstance(BiConsumer<ShopInstance<Impl>, Player> onInstance) {
            this.handleInstance = onInstance;
            return this;
        }

        public Builder<Impl> setSelectionScreenTitle(Function<ShopInstance<Impl>, Component> title) {
            this.selectionTitle = title;
            return this;
        }

        public Builder<Impl> setSelectionScreenSize(int size) {
            selectionSize = size;
            return this;
        }

        public Builder<Impl> setRemoveAmountButton(BiFunction<ShopInstance<Impl>, Integer, Component> name, BiFunction<ShopInstance<Impl>, Integer, List<Component>> description) {
            this.removeButtonName = name;
            this.removeButtonDescription = description;
            return this;
        }

        public Builder<Impl> setAddAmountButton(BiFunction<ShopInstance<Impl>, Integer, Component> name, BiFunction<ShopInstance<Impl>, Integer, List<Component>> description) {
            this.addButtonName = name;
            this.addButtonDescription = description;
            return this;
        }

//            public Builder<Impl> setPriceMessage(BiFunction<ShopInstance<Impl>, Integer, List<Component>> message) {
//                this.price = message;
//                return this;
//            }

        public ShopGuiTemplate<Impl> build() {
            Validate.isTrue(size != null, "Size must not be null");
            Validate.isTrue(title != null, "Title must not be null");
//                Validate.isTrue(price != null, "Price must not be null");
            Validate.isTrue(handleIcon != null, "Price must not be null");
            Validate.isTrue(handleInstance != null, "HandleInstance must not be null");
            Validate.isTrue(selectionTitle != null, "SelectionTitle must not be null");
            Validate.isTrue(selectionSize != null, "SelectionSize must not be null");
            Validate.isTrue(removeButtonName != null, "RemoveButtonName must not be null");
            Validate.isTrue(removeButtonDescription != null, "RemoveButtonDescription must not be null");
            Validate.isTrue(addButtonName != null, "AddButtonName must not be null");
            Validate.isTrue(addButtonDescription != null, "AddButtonDescription must not be null");
            Validate.isTrue(onBuy != null, "OnBuy must not be null");

            return new ShopGuiTemplate<>(this);
        }
    }

    public static final String SHOP_SUCCESSFULLY_PURCHASED_ITEM = "You successfully purchased {amount}x {item}";
    public static final String SHOP_NOT_ENOUGH_MONEY = "You do not have enough money to buy {amount}x {item}!";
    public static final String SHOP_NOT_ENOUGH_INVENTORY_SPACE = "<red>Not enough inventory space!";

    public static <T extends ShopItem<T>> ShopGuiTemplate<T> generic(Component title) {
        return new ShopGuiTemplate.Builder<T>(5 * 9, (shop) -> title)
                .onBuy(ShopGuiTemplate::onBuyFunction)
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

    private static <T extends ShopItem<T>> Component getPriceMessage(ShopInstance<T> instance, GuiIcon icon, int amount) {
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

    private static <T extends ShopItem<T>> void onBuyFunction(ShopTransactionResult<T> result, Player player) {
        switch (result.type()) {
            case NO_INVENTORY_SPACE -> player.sendMessage(SHOP_NOT_ENOUGH_INVENTORY_SPACE);
            case SUCCESS -> player.sendMessage(SHOP_SUCCESSFULLY_PURCHASED_ITEM.replace("{amount}", "" + result.amount())
                    .replace("{item}", result.instance().getItem().getName()));
            case NOT_ENOUGH_MONEY -> player.sendMessage(SHOP_NOT_ENOUGH_MONEY.replace("{amount}", "" +  result.amount())
                    .replace("{item}", result.instance().getItem().getName()));
            default -> throw new IllegalArgumentException("Unhandled TransactionResult: " + result.type());
        }
    }
}
