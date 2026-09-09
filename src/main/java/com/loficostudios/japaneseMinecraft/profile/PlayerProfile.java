package com.loficostudios.japaneseMinecraft.profile;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.Language;
import com.loficostudios.japaneseMinecraft.profile.info.GiftColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class PlayerProfile {
    private static final int DEFAULT_LIVES = 3;
    private static final double MAX_SANITY = 100; //100%
    private final File file;
    private final FileConfiguration config;

    private double money;

    private int lives;

//    @Configurable(name = "Pronouns")
    private String pronouns;

//    @Configurable(name = "Language", nullable = false)
    private Language language;

//    @Configurable(name = "UID", mutable = false)
    private final UUID uuid;

//    @Configurable(name = "Receive DMs")
    private boolean receiveDMs;

//    @Configurable(name = "Favorite Color")
    private GiftColor favoriteColor;

//    @Configurable(name = "Number Of Pets")
    private int numberOfPets;

    private double sanity;

    public PlayerProfile(JapaneseMinecraft plugin, Player player) {
        this(plugin, (OfflinePlayer) player);
    }

    public PlayerProfile(JapaneseMinecraft plugin, OfflinePlayer player) {
        this.uuid = player.getUniqueId();
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
        this.pronouns = config.getString("pronouns", "");
        this.lives = config.getInt("lives", DEFAULT_LIVES);
        this.money = config.getDouble("money", 0.0);
        this.numberOfPets = config.getInt("numberOfPets");

        this.receiveDMs = config.getBoolean("receiveDMs", true);

        /// Start at 100
        this.sanity = config.getDouble("sanity", MAX_SANITY);


        try {
            var string = config.getString("favoriteColor");
            this.favoriteColor = GiftColor.valueOf(string);
        } catch (Exception e) {
            this.favoriteColor = null;
        }

        try {
            var string = config.getString("language", "OTHER");
            this.language = Language.valueOf(string);
        } catch (IllegalArgumentException e) {
            if (player.isOnline() && player.getPlayer() != null) {
                var local = player.getPlayer().locale();
                var isJapanese = local.equals(Locale.JAPANESE) || local.equals(Locale.JAPAN);
                this.language = isJapanese ? Language.JAPANESE : Language.ENGLISH;
            } else {
                this.language = Language.ENGLISH;
            }
        }

        save();
    }

    public void setSanity(double sanity) {
        this.sanity = sanity;
    }

    public double getSanity() {
        return Math.min(sanity, MAX_SANITY);
    }

    public boolean hasMoney(double amount) {
        return money >= amount;
    }

    /// WILL OVERDRAW
    public void subtractMoney(double amount) {
        money -= amount;
        save();
    }

    public void addMoney(double amount) {
        money += amount;
        save();
    }

    public double getMoney() {
        return money;
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
        config.get("pronouns", pronouns);
        config.set("money", money);
        config.set("sanity", Math.min(sanity, MAX_SANITY));
        config.set("receiveDMs", receiveDMs);
        config.set("numberOfPets", numberOfPets);
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

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
}
