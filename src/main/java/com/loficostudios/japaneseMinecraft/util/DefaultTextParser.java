package com.loficostudios.japaneseMinecraft.util;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DefaultTextParser implements TextParser {

    @Override
    public @NotNull Component parseText(Player player, String text) {
        var mm = MiniMessage.miniMessage();

        /// handle internal placeholders first
        String[][] internal = {
                {"github_url", JapaneseMinecraft.SERVER_IP},
                {"player", player.getName()}
        };

        final char inner = '{';
        final char outer = '}';

        for (String[] strings : internal) {
            text = text.replace(
                    inner + strings[0] + outer, strings[1]
            );
        }

        /// handle external placeholders
        if (JapaneseMinecraft.isPlaceholderAPI()) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        try {
            return mm.deserialize(text);
        } catch (Exception e) {
            e.printStackTrace();
            return Component.text(text);
        }
    }
}
