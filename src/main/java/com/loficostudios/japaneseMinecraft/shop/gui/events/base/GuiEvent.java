package com.loficostudios.japaneseMinecraft.shop.gui.events.base;

import com.loficostudios.japaneseMinecraft.shop.gui.FloralGui;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class GuiEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public Player getPlayer() {
        return player;
    }

    public FloralGui getGui() {
        return gui;
    }

    private final Player player;

    private final FloralGui gui;

    private boolean cancelled;

    public GuiEvent(Player player, FloralGui gui) {
        this.player = player;
        this.gui = gui;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
