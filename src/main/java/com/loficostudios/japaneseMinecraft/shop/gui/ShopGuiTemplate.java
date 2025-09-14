package com.loficostudios.japaneseMinecraft.shop.gui;

import com.loficostudios.forgified.paper.gui.GuiIcon;
import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.shop.Shop;
import com.loficostudios.japaneseMinecraft.shop.ShopInstance;
import com.loficostudios.japaneseMinecraft.shop.ShopItem;
import com.loficostudios.japaneseMinecraft.shop.ShopTransactionResult;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
}
