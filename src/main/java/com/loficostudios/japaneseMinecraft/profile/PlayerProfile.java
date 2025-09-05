package com.loficostudios.japaneseMinecraft.profile;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.Language;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class PlayerProfile {
    private static final int DEFAULT_LIVES = 3;
    private final File file;
    private final FileConfiguration config;

    private int lives;
    private Language language;

    public PlayerProfile(JapaneseMinecraft plugin, Player player) {
        var file = new File(plugin.getDataFolder(), "players" + File.separator + player.getUniqueId() + ".yml");

        // Ensure parent directory exists
        file.getParentFile().mkdirs();

        // Create file if it does not exist
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
        this.lives = config.getInt("lives", DEFAULT_LIVES);

        try {
            var string = config.getString("language", "OTHER");
            this.language = Language.valueOf(string);
        } catch (IllegalArgumentException e) {
            var local = player.locale();
            var isJapanese = local.equals(Locale.JAPANESE) || local.equals(Locale.JAPAN);
            this.language = isJapanese ? Language.JAPANESE : Language.ENGLISH;
        }

        save();
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
        save();
    }

    public void save() {
        config.set("lives", lives);
        config.set("language", language.name());
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLives(int i) {
        lives = i;
        save();
    }

    public int getLives() {
        return lives;
    }
}
