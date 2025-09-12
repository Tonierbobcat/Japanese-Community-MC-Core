package com.loficostudios.japaneseMinecraft.shop.gui.events;

import com.loficostudios.japaneseMinecraft.shop.gui.FloralGui;
import com.loficostudios.japaneseMinecraft.shop.gui.GuiIcon;
import com.loficostudios.japaneseMinecraft.shop.gui.events.base.GuiEvent;
import org.bukkit.entity.Player;


public class GuiIconClickEvent extends GuiEvent {
    private final GuiIcon icon;
    public GuiIconClickEvent(Player player, FloralGui gui, GuiIcon icon) {
        super(player, gui);
        this.icon = icon;
    }

    public GuiIcon getIcon() {
        return icon;
    }
}
