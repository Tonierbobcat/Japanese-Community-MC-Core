package com.loficostudios.japaneseMinecraft.shop.gui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class MutableGui extends AbstractFloralGui {

    @Deprecated
    public MutableGui(int size, String title) {
        super(size, Component.text(title));
    }

    public MutableGui(int size, Component title) {
        super(size, title);
    }

    public MutableGui(int size) {
        super(size);
    }

    public abstract @NotNull Set<Integer> getMutableSlots();

    @Override
    public boolean setSlot(int slot, @NotNull GuiIcon icon) {
        if (getMutableSlots().contains(slot)) {
            throw new IllegalArgumentException("Slot is mutable!");
        }
        return super.setSlot(slot, icon);
    }
}
