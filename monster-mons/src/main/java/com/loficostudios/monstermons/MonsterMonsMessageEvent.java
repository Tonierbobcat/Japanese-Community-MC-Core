package com.loficostudios.monstermons;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MonsterMonsMessageEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private String message;
    private final String messageKey;
    private final @Nullable Player player;

    public MonsterMonsMessageEvent(String message, String messageKey, @Nullable Player player) {
        this.message = message;
        this.messageKey = messageKey;
        this.player = player;
    }

    public @Nullable Player getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
