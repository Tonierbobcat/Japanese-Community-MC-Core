package com.loficostudios.japaneseMinecraft;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class JapaneseMinecraft extends JavaPlugin implements CommandExecutor, TabCompleter {

    private static JapaneseMinecraft instance;

    private PlayerLocaleManager localeManager;

    private WeatherManager weatherManager;

    private ChatManager chatManager;

    public JapaneseMinecraft() {
        instance = this;
    }

    @Override
    public void onEnable() {
        localeManager = new PlayerLocaleManager();
        weatherManager = new WeatherManager();
        chatManager = new ChatManager();
        Arrays.asList(
                new PlayerListener(this), chatManager
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));

        this.getCommand("jpmc").setExecutor(this);
        this.getCommand("dm").setExecutor(new DMCommand(chatManager));
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(Component.text(Messages.getMessage(player, "suggestion_hint")));
                }
            }
        }.runTaskTimer(this, 0L, 120L * 60 * 5); // 5 minutes
    }

    @Override
    public void onDisable() {
    }

    public PlayerLocaleManager getLocaleManager() {
        return localeManager;
    }

    public WeatherManager getWeatherManager() {
        return weatherManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            if (args[0].equals("suggest")) {
                if (sender instanceof Player player) {
                    var suggestion = String.join(" ", args).substring(8).trim();
                    if (suggestion.isEmpty()) {
                        var message = Messages.getMessage(player, "cannot_submit_empty_suggestion");
                        player.sendMessage(Component.text(message));
                        return true;
                    }

                    var file = new File(getDataFolder(), "suggestions/" + System.currentTimeMillis() +"-" + player.getName() + ".txt");
                    file.getParentFile().mkdirs();

                    try {
                        if (file.createNewFile()) {
                            Files.writeString(file.toPath(), suggestion);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        var message = Messages.getMessage(player, "failed_to_submit_suggestion");
                        player.sendMessage(Component.text(message));
                        return true;
                    }

                    var message = Messages.getMessage(player, "successfully_submitted_suggestion");
                    player.sendMessage(Component.text(message));
                    return true;
                } else {
                    sender.sendMessage("This command can only be used by players.");
                    return true;
                }
            } else {
                sender.sendMessage("Unknown Command.");
                return true;
            }
        } else {
            sender.sendMessage("Usage: /jpmc suggest <your suggestion>");
            return true;
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("suggest");
        }
        return Collections.emptyList();
    }

    public static boolean isPlayerLanguageJapanese(Player player) {
        var language = instance.localeManager.getLanguage(player);
        return language == Language.JAPANESE;
    }

    public static @NotNull NamespacedKey getNMK(String key) {
        return new NamespacedKey(instance, key);
    }
}
