package com.loficostudios.japaneseMinecraft;

import com.loficostudios.japaneseMinecraft.tasks.DeathEffectParticles;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;

public class PlayerListener implements Listener {



    private static final String GITHUB_URL = "https://github.com/Tonierbobcat/Japanese-Community-MC-Core";
    private static final String DISCORD_URL = "discord.gg/YS8ZXeAwnB";
    private static final String SERVER_IP = "jp.loficostudios.com";

    private static final String JOIN_MESSAGE = "§a§l+ §f<player>";
    private static final String QUIT_MESSAGE = "§c§l- §f<player>";

    private static final boolean PLAY_GLOBAL_REVIVED_MESSAGE = false;

    private final BossBar overlay;

    private final Map<UUID, BukkitTask> deathTasks = new HashMap<>();

    private final JapaneseMinecraft plugin;

    public PlayerListener(JapaneseMinecraft plugin) {
        this.plugin = plugin;

        String[] lines = {
                "!!! JOIN NOW !!!",
                "@ " + SERVER_IP,
                "!!! DISCORD !!!",
                "@ " + DISCORD_URL
        };

        overlay = Bukkit.createBossBar(lines[0], BarColor.BLUE, BarStyle.SOLID);
        new BukkitRunnable() {
            int index = 0;
            @Override
            public void run() {
                overlay.setTitle(lines[index]);
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

                    player.sendActionBar(Component.text(livesText + " §r| " + timeText + " §r| " + symbol + " §f" + plugin.getWeatherManager().getTemperature(player) + "°C"));
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
    private void onQuit(PlayerQuitEvent e) {
        e.quitMessage(Component.text(QUIT_MESSAGE.replace("<player>", e.getPlayer().getName())));
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();

        e.joinMessage(Component.text(JOIN_MESSAGE.replace("<player>", player.getName())));

        handlePlayerLocale(player);

        overlay.addPlayer(player);

        if (player.getGameMode().equals(GameMode.SURVIVAL))
            player.setInvulnerable(false);
        Optional.ofNullable(player.getAttribute(Attribute.MOVEMENT_SPEED)).ifPresent((instance) -> {
            instance.removeModifier(JapaneseMinecraft.getNMK("death_speed_boost"));
        });

        List<Consumer<Player>> notifications = List.of(
                (p) -> {
                    var lines = JapaneseMinecraft.isPlayerLanguageJapanese(p) ? Messages.JAPANESE_WELCOME_MESSAGE : Messages.ENGLISH_WELCOME_MESSAGE;

                    for(String line : lines) {
                        p.sendMessage(line.replace("<player>", p.getName()));
                    }
                    p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.75f);
                }, (p) -> {
                    var message = Messages.getMessage(player, "github_hint");
                    player.sendMessage(message);
                    player.sendMessage(Component.text().append(Component.text("Github:")).append(Component.text(" "))
                            .append(Component.text().append(Component.text("Japanese-Community-MC-Core", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl(GITHUB_URL)));

                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.75f);
                }
        );

        /// TODO Move these to a notification manager class

        new BukkitRunnable() {
            @Override
            public void run() {
                if (notifications.isEmpty())
                    return;

                player.sendMessage(" ");
                notifications.getFirst().accept(player);

                new BukkitRunnable() {
                    int index = 1;
                    final int max = notifications.size();
                    @Override
                    public void run() {
                        if (index >= max) {
                            this.cancel();
                            return;
                        }

                        player.sendMessage(" ");
                        notifications.get(index).accept(player);

                        index++;
                    }
                }.runTaskTimer(plugin, 20L*10L, 20L*10L);
            }
        }.runTaskLater(plugin, 30L);
    }



    private void handlePlayerLocale(Player player) {
        var local = player.locale();
        var isJapanese = local.equals(Locale.JAPANESE) || local.equals(Locale.JAPAN);
        plugin.getLocaleManager().setLanguage(player, isJapanese ? Language.JAPANESE : Language.ENGLISH);
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
