package com.loficostudios.japaneseMinecraft.shop.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface FloralGui extends InventoryHolder {
    boolean open(@NotNull Player player);
    boolean close(@NotNull Player player);

    default void create(Player player) {
    }

    GuiIcon getIcon(int slot);

    default ItemStack getSlot(int slot) {
        return getInventory().getItem(slot);
    }

    boolean setSlot(int slot, GuiIcon icon);
    boolean setSlot(int slot, ItemStack item);

    static int validateSize(int size) {
        final Set<Integer> allowedInventorySize = new HashSet<>(Set.of(
                9, 18, 27, 36, 45, 54
        ));

        return allowedInventorySize.stream()
                .filter(allowedSize -> allowedSize >= size)
                .min(Integer::compareTo)
                .orElse(9);
    }

    static List<Integer> getPerimeter(int rows, int columns) {
        Set<Integer> perimeterIndexes = new LinkedHashSet<>();

        for (int i = 0; i < columns; i++) {
            perimeterIndexes.add(i);
        }

        for (int i = (rows * columns) - columns; i < rows * columns; i++) {
            perimeterIndexes.add(i);
        }

        for (int i = 0; i < rows; i++) {
            perimeterIndexes.add(i * columns);
        }

        for (int i = 0; i < rows; i++) {
            perimeterIndexes.add((i * columns) + (columns - 1));
        }

        return new ArrayList<>(perimeterIndexes);
    }
}
