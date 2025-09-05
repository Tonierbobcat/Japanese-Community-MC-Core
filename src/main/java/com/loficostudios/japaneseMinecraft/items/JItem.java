package com.loficostudios.japaneseMinecraft.items;

import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class JItem {
    private final String id;
    private final Function<String, ItemStack> factory;

    public JItem(String id, Function<String, ItemStack> factory) {
        this.id = id;
        this.factory = factory;
    }

    public ItemStack getItemStack(int amount) {
        var item = factory.apply(id);
        item.setAmount(amount);
        return item;
    }

    public boolean isItem(ItemStack item) {
        var id = Items.getItemId(item);
        return this.id.equals(id);
    }
}
