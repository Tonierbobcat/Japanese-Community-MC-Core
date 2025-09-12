package com.loficostudios.japaneseMinecraft.shop.gui.events;

import com.loficostudios.japaneseMinecraft.shop.gui.FloralGui;
import com.loficostudios.japaneseMinecraft.shop.gui.events.base.GuiEvent;
import org.bukkit.entity.Player;

public class GuiCloseEvent extends GuiEvent {
    public GuiCloseEvent(Player player, FloralGui gui) {
        super(player, gui);
    }
}
