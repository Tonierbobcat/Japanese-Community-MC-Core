package com.loficostudios.japaneseMinecraft;

import com.loficostudios.forgified.paper.IPluginResources;
import com.loficostudios.forgified.paper.utils.ResourceLoadingUtils;
import com.loficostudios.japaneseMinecraft.chat.ChatManager;
import com.loficostudios.japaneseMinecraft.commands.*;
import com.loficostudios.japaneseMinecraft.economy.DefaultEconomy;
import com.loficostudios.japaneseMinecraft.economy.VaultEconomy;
import com.loficostudios.japaneseMinecraft.games.Game;
import com.loficostudios.japaneseMinecraft.games.GameManager;
import com.loficostudios.japaneseMinecraft.games.KakurenboGame;
import com.loficostudios.japaneseMinecraft.games.shiritori.ShiritoriGame;
import com.loficostudios.japaneseMinecraft.listener.ItemListener;
import com.loficostudios.japaneseMinecraft.listener.MobListener;
import com.loficostudios.japaneseMinecraft.listener.PlayerDeathListener;
import com.loficostudios.japaneseMinecraft.listener.PlayerListener;
import com.loficostudios.japaneseMinecraft.notifications.NotificationManager;
import com.loficostudios.japaneseMinecraft.pokemon.MonsterBallListener;
import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;
import com.loficostudios.japaneseMinecraft.profile.ProfileManager;
import com.loficostudios.japaneseMinecraft.sanity.SanityManager;
import com.loficostudios.japaneseMinecraft.shop.EconomyProvider;
import com.loficostudios.japaneseMinecraft.util.DefaultTextParser;
import com.loficostudios.japaneseMinecraft.util.TextParser;
import com.loficostudios.townsplugin.api.HarmonizedTownsAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
    private final TextParser textParser = new DefaultTextParser();

    private ProfileManager profileManager;

    private GameManager gameManager;

    private EconomyProvider economy;

    private HarmonizedTownsAPI townsAPI;


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

        if (!initializeTownsAPI()) {
            getLogger().severe("Failed to initialize Towns API. Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            Messages.initialize(this);
        } catch (IOException e) {
            e.printStackTrace();
            Debug.logError("Could not load messages. " + e.getMessage());
            Debug.logError("Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        Items.ITEMS.initialize(this);

        /// tbh this is pretty clean
        // DISABLED for now until I update recipe loaders in library
//        RecipeLoaderRegistry.getBukkitRecipeLoaderRegistry()
//                .initialize(this);

        /// this should get called after initializing items and recipes
        ResourceLoadingUtils.generateResourcePack(this);

        /// Initializes managers
        weatherManager = new WeatherManager();
        chatManager = new ChatManager(this);
        notificationManager = new NotificationManager(this, textParser);
        profileManager = new ProfileManager(this);

        /// initialize economy after profile manager loads
        setupEconomy();

        /// setup registered games like shiritori and kakurenbo
        setupGames();

        // stub for now
        new SanityManager();

        /// Register event listeners
        registerEvents();

        /// Register commands
        registerCommands();

        /// Start announcement task
        //todo move this to notification manager
        startAnnouncementTask();
    }

    private void setupGames() {
         List<Game> games = List.of(
                new ShiritoriGame(2),
                new KakurenboGame()
        );
        games.forEach(game -> {
            if (game instanceof Listener)
                Bukkit.getPluginManager().registerEvents(((Listener) game), this);
        });

        /// Initialize GameManager with registered games
        gameManager = new GameManager(games);
    }

    public boolean initializeTownsAPI() {
        var rsp = getServer().getServicesManager().getRegistration(HarmonizedTownsAPI.class);
        if (rsp == null)
            return false;
        townsAPI = rsp.getProvider();

        /// setup config
        setupTownsAPIConfig();
        return true;
    }

    private void setupTownsAPIConfig() {
        /// only one town per player to save resources
        /// towns are projected to be relatively heavy on proformance
        int TOWNS_PER_PLAYER = 1;
        int CLAIMS_PER_PLAYER = 20;

        int MAX_TOWN_SIDE = 250;
        int MIN_TOWN_SIDE = 50;

        int MIN_TOWN_AREA = MIN_TOWN_SIDE * MIN_TOWN_SIDE;

        int MIN_CLAIM_SIDE = 10;
        int MIN_CLAIM_AREA = MIN_CLAIM_SIDE * MIN_CLAIM_SIDE;

        /// default towns per player is 1 to save resources and the max area is 250x250 so people don't hog space
        townsAPI.getAPIConfig()
                .prefix(Common.createMessagePrefix("Towns", "§e"))
                .setMessagingOverride((key, player) -> Messages.getMessage(player, key))

                /// town creation restrictions are pretty consistant throughout
                .setMinTownLength(MIN_TOWN_SIDE)
                .setMinTownWidth(MIN_TOWN_SIDE)
                .setMaxTownLength(MAX_TOWN_SIDE)
                .setMaxTownWidth(MAX_TOWN_SIDE)

                .setDefaultTownBlocks(MAX_TOWN_SIDE*MAX_TOWN_SIDE)

                .setMaxTownsPerPlayer(TOWNS_PER_PLAYER)

                /// players can have up to 20 claims
                .setMaxClaimsPerPlayer(20)

                .setMinClaimBlocks(MIN_CLAIM_AREA)

                /// the biggest claims are the smallest towns
                /// this is on a per-claim basis
                .setMaxClaimBlocks(MIN_TOWN_AREA)

                .setDefaultClaimBlocks(MIN_TOWN_AREA*CLAIMS_PER_PLAYER);
    }

    public void setupEconomy() {
        EconomyProvider provider;
        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
            var vaultEconomy = new VaultEconomy(this);
            getServer().getServicesManager().register(Economy.class, vaultEconomy, this, ServicePriority.Normal);
            provider = vaultEconomy;
            Debug.log("Registered vault economy to ServicesManager");
        } catch (ClassNotFoundException e) {
            /// we still create a provider, we are just not registering it with RSP
            Debug.logWarning("Vault not installed. Could not register economy to ServicesManager.");
            provider = new DefaultEconomy();
        }
        this.economy = provider;
    }

    /// this is where independent listeners should go
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
        /// rather than a static instance. it should be stored in a field in this class
        ItemListener.clearLightSources();
    }

    public WeatherManager getWeatherManager() {
        return weatherManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    @Override
    public File getJarFile() {
        return getFile();
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public EconomyProvider getEconomy() {
        return economy;
    }

    public TextParser getTextParser() {
        return textParser;
    }

    public static HarmonizedTownsAPI getTownsAPI() {
        return instance.townsAPI;
    }

    public static EconomyProvider getEconomyProvider() {
        return instance.getEconomy();
    }

    public static boolean isPlayerLanguageJapanese(Player player) {
        var language = instance.profileManager.getProfile(player).getLanguage();
        return language == Language.JAPANESE;
    }

    public static PlayerProfile getPlayerProfile(Player player) {
        return instance.profileManager.getProfile(player);
    }

//    public static Component parseText(Player player, String text) {
//        return instance.getTextParser().parseText(player, text);
//    }

    public static BukkitTask runTaskTimer(Runnable runnable, long delay, long ticks) {
        if (runnable instanceof BukkitRunnable) {
            return ((BukkitRunnable) runnable).runTaskTimer(instance, delay, ticks);
        } else {
            return instance.getServer().getScheduler().runTaskTimer(instance, runnable, delay, ticks);
        }
    }

    public static boolean isPlaceholderAPI() {
        return instance.placeholderAPIEnabled;
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
}
