package com.loficostudios.japaneseMinecraft;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class DummyItem implements Consumer<ItemStack> {

    @Override
    public void accept(ItemStack stack) {
        var meta = stack.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer()
                .set(new NamespacedKey("japaneseminecraft", "dummy"), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
    }
}

