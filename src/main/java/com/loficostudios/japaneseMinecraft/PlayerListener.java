package com.loficostudios.japaneseMinecraft;

import com.loficostudios.japaneseMinecraft.tasks.DeathEffectParticles;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PlayerListener implements Listener {

    private static final String[] ENGLISH_WELCOME_MESSAGE = {
            "This is a community server. Please be respectful to others.",
            "This server is a work in progress. Features may be added or changed over time.",
            "If you have any suggestions, please use /jpmc suggest <your suggestion>",
            "Enjoy your time here!",
            " ",
            " - DEV NOTE. I am able to code / add any features you may want."
    };

    private static final String[] JAPANESE_WELCOME_MESSAGE = {
            "ここはコミュニティサーバーです。他の人に敬意を持って接してください。",
            "このサーバーは進行中のプロジェクトです。機能は時間とともに追加または変更される場合があります。",
            "ご提案がある場合は、/jpmc suggest <あなたの提案> を使用してください。",
            "ここでの時間を楽しんでください！",
            " ",
            " - 開発者ノート 追加の注意として、私はあなたが望むかもしれない機能をコード化/追加することができます。"
    };

    private static final boolean PLAY_GLOBAL_REVIVED_MESSAGE = false;

    private final BossBar gameBar;

    private final Map<UUID, BukkitTask> deathTasks = new HashMap<>();

    private final JapaneseMinecraft plugin;

    public PlayerListener(JapaneseMinecraft plugin) {
        this.plugin = plugin;

        String[] lines = {
                "!!! JOIN NOW !!!",
                "@ jp.loficostudios.com",
                "!!! DISCORD !!!",
                "@ discord.gg/YS8ZXeAwnB"
        };

        gameBar = Bukkit.createBossBar(lines[0], BarColor.BLUE, BarStyle.SOLID);
        new BukkitRunnable() {
            int index = 0;
            @Override
            public void run() {
                gameBar.setTitle(lines[index]);
                index = (index + 1) % lines.length;
            }
        }.runTaskTimer(plugin, 0L, 45L);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    var wrld = player.getWorld();
                    var time = wrld.getTime();
                    var shifted = (time + 6000) % 24000;
                    var timeText = "§e⌚ §f" + String.format("%02d:%02d", shifted / 1000, ((shifted % 1000) * 60) / 1000);
                    var livesText = "§a❤ §f" + getLives(player);

                    Map<WeatherManager.WeatherType, String> symbols = Map.of(
                            WeatherManager.WeatherType.SUNNY, wrld.isDayTime() ? "§e☀" : "§f☽",
                            WeatherManager.WeatherType.CLOUDY, "§7☁",
                            WeatherManager.WeatherType.STORMY, "§e⚡",
                            WeatherManager.WeatherType.RAINY, "§9☂",
                            WeatherManager.WeatherType.SNOWY, "§b❄"
                    );

                    var type = plugin.getWeatherManager().getWeatherType(player);

                    var symbol = symbols.getOrDefault(type, "<null>");

                    player.sendActionBar(Component.text(livesText + "§r | " + timeText + "§r | " + symbol + " " + plugin.getWeatherManager().getTemperature(player) + "°C"));
                }
            }
        }.runTaskTimer(plugin, 0L, 2);
    }

    private final Map<UUID, Integer> playerLives = new HashMap<>();

    private int getLives(Player player) {
        return playerLives.computeIfAbsent(player.getUniqueId(), k -> 3);
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent e) {
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setDroppedExp(0);
    }

    @EventHandler
    private void onRespawn(PlayerRespawnEvent e) {
        playerLives.put(e.getPlayer().getUniqueId(), 3);
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player))
            return;
        var causing = e.getDamageSource().getCausingEntity();
        var causedByPlayer = causing instanceof Player;
        if (causedByPlayer) {
            e.setCancelled(true);
            return;
        }

        boolean isDeath = e.getFinalDamage() >= player.getHealth();
        if (!isDeath)
            return;

        var canRevive = getLives(player) > 1;
        if (canRevive) {
            playerLives.put(player.getUniqueId(), getLives(player) - 1);
        } else {
            return;
        }
        e.setCancelled(true);
        playReviveEffect(player);
    }

    @EventHandler
    private void onServerPing(ServerListPingEvent e) {
        Component motd = Component.text("A Japanese Community Server!", NamedTextColor.GREEN)
                .append(Component.text("\nSHIRITORI COMING SOON!!!", NamedTextColor.YELLOW));
        e.motd(motd);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();

        handlePlayerLocale(player);

        var message = String.format(Messages.getMessage(player, "welcome"), player.getName());

        Common.notify(player, message);

        gameBar.addPlayer(player);

        if (player.getGameMode().equals(GameMode.SURVIVAL))
            player.setInvulnerable(false);
        Optional.ofNullable(player.getAttribute(Attribute.MOVEMENT_SPEED)).ifPresent((instance) -> {
            instance.removeModifier(JapaneseMinecraft.getNMK("death_speed_boost"));
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                sendWelcomeMessage(player);
            }
        }.runTaskLater(plugin, 30L);
    }

    private void handlePlayerLocale(Player player) {
        var local = player.locale();
        var isJapanese = local.equals(Locale.JAPANESE) || local.equals(Locale.JAPAN);
        plugin.getLocaleManager().setLanguage(player, isJapanese ? Language.JAPANESE : Language.ENGLISH);
    }

    private void sendWelcomeMessage(Player player) {
        var githubLink = "jp.loficostudios.com";
        var githubLinkText = Component.text().clickEvent(ClickEvent.openUrl(githubLink));

        var lines = JapaneseMinecraft.isPlayerLanguageJapanese(player) ? JAPANESE_WELCOME_MESSAGE : ENGLISH_WELCOME_MESSAGE;
        for(String line : lines) {
            player.sendMessage(line);
        }
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.75f);
    }

    private void playReviveEffect(Player player) {
        var maxHealth = player.getAttribute(Attribute.MAX_HEALTH);

        int effectTime = 60;

        player.setExp(0);
        player.setLevel(0);

        player.setHealth(maxHealth != null ? maxHealth.getValue() : 20.0);

        if (PLAY_GLOBAL_REVIVED_MESSAGE)
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(String.format(Messages.getMessage(p, "global_player_revived"), player.getName()));
            }

        player.sendMessage(Messages.getMessage(player, "player_revived"));
        player.sendMessage(Messages.getMessage(player, "you_lost_experience"));

        player.playSound(player, Sound.ENTITY_CAT_HURT, 0.5f, 1);
        player.playSound(player, Sound.ITEM_TOTEM_USE, 0.4f, 1);

        var existingTask = deathTasks.get(player.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
        }

        int particleInterval = 4;
        deathTasks.put(player.getUniqueId(), new DeathEffectParticles(player, effectTime / particleInterval)
                .runTaskTimer(plugin, 0, particleInterval));

        /// Modify player states
        player.setInvulnerable(true);

        Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(plugin, player));
//        player.setInvisible(true);

        /// Wrapping in optional to avoid writing null checks
        Optional.ofNullable(player.getAttribute(Attribute.MOVEMENT_SPEED)).ifPresent((instance) -> {
            try {
                instance.addModifier(new AttributeModifier(JapaneseMinecraft.getNMK("death_speed_boost"), 0.8, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
            } catch (IllegalArgumentException ignored) {
            }
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                /// Unsetting modified player states

                player.setInvulnerable(false);

                Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, player));
//                player.setInvisible(false);

                /// Wrapping in optional to avoid writing null checks
                Optional.ofNullable(player.getAttribute(Attribute.MOVEMENT_SPEED)).ifPresent((instance) -> {
                    instance.removeModifier(JapaneseMinecraft.getNMK("death_speed_boost"));
                });
            }
        }.runTaskLater(plugin, 60L);
    }
}
