package com.loficostudios.japaneseMinecraft.shop.gui;

import com.loficostudios.japaneseMinecraft.shop.ShopInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.function.BiConsumer;

public interface ShopGuiIcon {
    GuiIcon getIcon(ShopInstance<?> instance, int amount, BiConsumer<Player, ClickType> onClick);
}
