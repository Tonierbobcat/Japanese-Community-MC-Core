package com.loficostudios.japaneseMinecraft;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerLocaleManager {
    private final HashMap<UUID, Language> languages = new HashMap<>();

    public Language getLanguage(Player player) {
        return languages.getOrDefault(player.getUniqueId(), Language.OTHER);
    }

    public void setLanguage(Player player, Language language) {
        languages.put(player.getUniqueId(), language);
    }
}
