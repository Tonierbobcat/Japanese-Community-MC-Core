package com.loficostudios.japaneseMinecraft;

import com.loficostudios.forgified.paper.IPluginResources;
import com.loficostudios.forgified.paper.utils.ResourceLoadingUtils;
import com.loficostudios.japaneseMinecraft.chat.ChatManager;
import com.loficostudios.japaneseMinecraft.commands.*;
import com.loficostudios.japaneseMinecraft.games.shiritori.ShiritoriManager;
import com.loficostudios.japaneseMinecraft.listener.ItemListener;
import com.loficostudios.japaneseMinecraft.listener.MobListener;
import com.loficostudios.japaneseMinecraft.listener.PlayerDeathListener;
import com.loficostudios.japaneseMinecraft.listener.PlayerListener;
import com.loficostudios.japaneseMinecraft.notifications.NotificationManager;
import com.loficostudios.japaneseMinecraft.pokemon.MonsterBallListener;
import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;
import com.loficostudios.japaneseMinecraft.profile.ProfileManager;
import com.loficostudios.japaneseMinecraft.sanity.SanityManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class JapaneseMinecraft extends JavaPlugin implements IPluginResources {

    /// DO NOT CHANGE THESE VALUES
    public static final String GITHUB_URL = "https://github.com/Tonierbobcat/Japanese-Community-MC-Core";
    public static final String DISCORD_URL = "discord.gg/YS8ZXeAwnB";
    public static final String SERVER_IP = "jp.loficostudios.com";

    ///  MOTD can contain color codes and \n for new lines
    public static final String MOTD = "§eA Japanese Community Server!\n§aNEW POKEMON SYSTEM!!!";

    private static JapaneseMinecraft instance;

    private WeatherManager weatherManager;

    private ChatManager chatManager;

    private NotificationManager notificationManager;

    private boolean placeholderAPIEnabled = false;

    private ProfileManager profileManager;

    private ShiritoriManager shiritoriManager;

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

        Items.ITEMS.initialize(this);

        /// tbh this is pretty clean
        // DISABLED for now until I update recipe loaders in library
//        RecipeLoaderRegistry.getBukkitRecipeLoaderRegistry()
//                .initialize(this);

        ResourceLoadingUtils.generateResourcePack(this);

        /// Initializes managers
        weatherManager = new WeatherManager();
        chatManager = new ChatManager(this);
        notificationManager = new NotificationManager();
        profileManager = new ProfileManager(this);
        shiritoriManager = new ShiritoriManager(this);

        // stub for now
        new SanityManager();

        /// Register event listeners
        registerEvents();

        /// Register commands
        registerCommands();

        /// Start announcement task
        startAnnouncementTask();

    }

    private void registerEvents() {
        Arrays.asList(
                new PlayerListener(this), new MobListener(), new PlayerDeathListener(this), new MonsterBallListener(this), new ItemListener()
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    private void startAnnouncementTask() {
        /// Every 5 minutes, notify players about /jpmc suggest
        runTaskTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(Component.text(Messages.getMessage(player, "suggestion_hint")));
            }
        }, 0L, 120L * 60 * 5); // 5 minutes
    }

    private void registerCommands() {
        Map.of(
                "jpmc", new JPMCCommand(this),
                "dm", new DMCommand(chatManager),
                "home", new HomeCommand(),
                "spicify", new SpicifyCommand(this),
                "fly", new FlyCommand(),
                "shop", new ShopCommand()
        ).forEach((id, executor) -> Optional.ofNullable(this.getCommand(id))
                .ifPresentOrElse(command -> command.setExecutor(executor), () -> Debug.log("Failed to register command: " + id)));
    }

    @Override
    public void onDisable() {

        /// This should be handled better
        /// rather then a static instance. it should be stored in a field in this class
        ItemListener.clearLightSources();
    }

    public WeatherManager getWeatherManager() {
        return weatherManager;
    }

    public ShiritoriManager getShiritoriManager() {
        return shiritoriManager;
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

    public static Component parseText(Player player, String text) {
        var mm = MiniMessage.miniMessage();

        /// handle internal placeholders first
        String[][] internal = {
                {"github_url", SERVER_IP},
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

    public static BukkitTask runTaskTimer(Runnable runnable, long delay, long ticks) {
        if (runnable instanceof BukkitRunnable) {
            return ((BukkitRunnable) runnable).runTaskTimer(instance, delay, ticks);
        } else {
            return instance.getServer().getScheduler().runTaskTimer(instance, runnable, delay, ticks);
        }
    }

    public static BukkitTask runTaskTimer(Consumer<BukkitRunnable> runnable, long delay, long ticks) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                runnable.accept(this);
            }
        }.runTaskTimer(instance, delay, ticks);
    }

    public static BukkitTask runTask(Runnable runnable) {
        if (runnable instanceof BukkitRunnable) {
            return ((BukkitRunnable) runnable).runTask(instance);
        } else {
            return instance.getServer().getScheduler().runTask(instance, runnable);
        }
    }

    public static BukkitTask runTaskLater(Runnable runnable , long delay) {
        if (runnable instanceof BukkitRunnable) {
            return ((BukkitRunnable) runnable).runTaskLater(instance, delay);
        } else {
            return instance.getServer().getScheduler().runTaskLater(instance, runnable, delay);
        }
    }

    public static BukkitTask runTaskAsynchronously(Runnable runnable) {
        if (runnable instanceof BukkitRunnable) {
            return ((BukkitRunnable) runnable).runTaskAsynchronously(instance);
        } else {
            return instance.getServer().getScheduler().runTaskAsynchronously(instance, runnable);
        }
    }

    @Override
    public File getJarFile() {
        return getFile();
    }
}
