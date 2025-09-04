package com.loficostudios.japaneseMinecraft;

import com.loficostudios.japaneseMinecraft.commands.DMCommand;
import com.loficostudios.japaneseMinecraft.commands.JPMCCommand;
import com.loficostudios.japaneseMinecraft.listener.MobListener;
import com.loficostudios.japaneseMinecraft.listener.PlayerDeathListener;
import com.loficostudios.japaneseMinecraft.listener.PlayerListener;
import com.loficostudios.japaneseMinecraft.notifications.NotificationManager;
import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;
import com.loficostudios.japaneseMinecraft.profile.ProfileManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class JapaneseMinecraft extends JavaPlugin {

    /// DO NOT CHANGE THESE VALUES
    public static final String GITHUB_URL = "https://github.com/Tonierbobcat/Japanese-Community-MC-Core";
    public static final String DISCORD_URL = "discord.gg/YS8ZXeAwnB";
    public static final String SERVER_IP = "jp.loficostudios.com";

    ///  MOTD can contain color codes and \n for new lines
    public static final String MOTD = "§eA Japanese Community Server!\n§aSHIRITORI COMING SOON!!!";

    private static JapaneseMinecraft instance;

    private WeatherManager weatherManager;

    private ChatManager chatManager;

    private NotificationManager notificationManager;

    private boolean placeholderAPIEnabled = false;

    private ProfileManager profileManager;

    public JapaneseMinecraft() {
        instance = this;
    }

    @Override
    public void onEnable() {

        /// check for PlaceholderAPI
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            placeholderAPIEnabled = true;
        } catch (ClassNotFoundException ignore) {
        }

        /// Initializes managers
        weatherManager = new WeatherManager();
        chatManager = new ChatManager();
        notificationManager = new NotificationManager();
        profileManager = new ProfileManager(this);

        /// Register event listeners
        registerEvents();

        /// Register commands
        registerCommands();

        /// Start announcement task
        startAnnouncementTask();
    }

    private void registerEvents() {
        Arrays.asList(
                new PlayerListener(this), chatManager, profileManager, new MobListener(), new PlayerDeathListener(this)
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    private void startAnnouncementTask() {
        /// Every 5 minutes, notify players about /jpmc suggest
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(Component.text(Messages.getMessage(player, "suggestion_hint")));
                }
            }
        }.runTaskTimer(this, 0L, 120L * 60 * 5); // 5 minutes
    }

    private void registerCommands() {
        Map.of(
                "jpmc", new JPMCCommand(this),
                "dm", new DMCommand(chatManager)
        ).forEach((id, executor) -> Optional.ofNullable(this.getCommand(id))
                .ifPresentOrElse(command -> command.setExecutor(executor), () -> Debug.log("Failed to register command: " + id)));
    }

    @Override
    public void onDisable() {
    }

    public WeatherManager getWeatherManager() {
        return weatherManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public static boolean isPlayerLanguageJapanese(Player player) {
        var language = instance.profileManager.getProfile(player).getLanguage();
        return language == Language.JAPANESE;
    }

    public static PlayerProfile getPlayerProfile(Player player) {
        return instance.profileManager.getProfile(player);
    }

    public static @NotNull NamespacedKey getNMK(String key) {
        return new NamespacedKey(instance, key);
    }

    public static Component parseText(Player player, String text) {
        var mm = MiniMessage.miniMessage();

        /// handle internal placeholders first
        List<Function<String, String>> internalPlaceholders = List.of(
                (t) -> t.replace("%github_url%", GITHUB_URL),
                (t) -> t.replace("%player%", player.getName())
        );

        for (Function<String, String> internalPlaceholder : internalPlaceholders) {
            text = internalPlaceholder.apply(text);
        }

        /// handle external placeholders
        if (instance.placeholderAPIEnabled) {
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
