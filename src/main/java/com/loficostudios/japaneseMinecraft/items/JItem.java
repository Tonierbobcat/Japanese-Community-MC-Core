package com.loficostudios.japaneseMinecraft.items;

import org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class JItem {
    private final String id;
    private final Function<JItem, ItemStack> factory;

    public JItem(String id, Function<JItem, ItemStack> factory) {
        Validate.isTrue(id != null && !id.isEmpty(), "id cannot be null or empty");
        this.id = id;
        this.factory = factory;
    }

    public ItemStack getItemStack(int amount) {
        var item = factory.apply(this);
        item.setAmount(amount);
        return item;
    }

    public boolean isItem(ItemStack item) {
        var id = ItemRegistry.getItemId(item);
        return this.id.equals(id);
    }

    public String getId() {
        return id;
    }
}
