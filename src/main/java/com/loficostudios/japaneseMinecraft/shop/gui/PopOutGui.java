package com.loficostudios.japaneseMinecraft.shop.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class PopOutGui extends AbstractFloralGui {
    private final Consumer<Player> onClose;

    public PopOutGui(int size, Component title, Consumer<Player> onClose) {
        super(size, title);
        this.onClose = onClose;
    }

    public void onClose(Player player) {
        if (onClose != null) {
            onClose.accept(player);
        }
    }
}
