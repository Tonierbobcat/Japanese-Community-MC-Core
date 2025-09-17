package com.loficostudios.japaneseMinecraft.util;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface TextParser {
    @NotNull Component parseText(Player player, String text);
}
