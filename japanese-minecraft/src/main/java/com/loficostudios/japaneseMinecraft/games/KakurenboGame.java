package com.loficostudios.japaneseMinecraft.games;


import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// Hide and seek
public class KakurenboGame implements Game, Listener {

    public static final String PREFIX = Common.createMessagePrefix("Kakurenbo", "§9");
    private static final int HIDER_GRACE_PERIOD_SECONDS = 30;

    private enum KakurenboStopReason { TIMEOUT, NO_HIDERS }
    private boolean active;

    private final List<Player> seekers = new ArrayList<>();
    private final List<Player> hiders = new ArrayList<>();
    private BukkitTask task;
    private int hidersAtStart;

    @Override
    public int getLengthMinutes() {
        return 1;
    }

    @Override
    public void reset() {
        hidersAtStart = 0;
        hiders.clear();
        seekers.clear();
    }

    /// This is only called when the game times out
    @Override
    public void end() {
        end(KakurenboStopReason.TIMEOUT);
    }

    private boolean isSeeker(Player player) {
        return seekers.contains(player);
    }

    private boolean isHider(Player player) {
    return hiders.contains(player);
    }

    private void convertToSeeker(Player player, Player catcher) {
    hiders.remove(player);
    notifyPlayer(player, "You have been caught by {catcher}!".replace("{catcher}", catcher.getName()));
    makeSeeker(player);
    }

    private void initializePlayers() {
     List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
     Collections.shuffle(players);

     int playerCount = players.size();
     if (playerCount > 1) {
         makeSeeker(players.getFirst());
     }
     if (playerCount > 3) {
         makeSeeker(players.get(1));
     }
     for (Player player : players) {
         if (isSeeker(player))
             continue;
         makeHider(player);
     }
    }

    /// [The length of the game is the length of this song](https://musescore.com/user/54937749/scores/11525008)
    @Override
    public void start() {
        active = true;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage("""
                    ---------- {prefix} ------------
                    In this game players are split into two teams
                    hiders & seekers. both teams get equal rewards
                    the top 3 seekers with the most catches gets rewards
                    hiders share a $1000 per player hider reward pool
                    hiders have {grace-period-seconds}s to hide
                    -------------------------------
                    """.replace("{prefix}", PREFIX).replace("{grace-period-seconds}", "" + HIDER_GRACE_PERIOD_SECONDS));
        }

        initializePlayers();

        hidersAtStart = hiders.size();
        task = JapaneseMinecraft.runTaskTimer(() -> {
            /// end game if there are no more hiders
            if (hiders.isEmpty()) {
                end(KakurenboStopReason.NO_HIDERS);
            }
        }, 0, 10);
    }

    private void end(KakurenboStopReason reason) {
        if (!active)
            return;
        active = false;
        if (task != null)
            task.cancel();
        for (Player player : Bukkit.getOnlinePlayers()) {
            notifyPlayer(player, "The game is over. " + Common.formatEnumName(reason));
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onTag(EntityDamageByEntityEvent e) {
        Debug.log("onTag");
        var victimEntity = e.getEntity();
        var attackerEntity = e.getDamager();
        if (!(victimEntity instanceof Player)) {
            Debug.log("victim is not player");
            return;
        }

        if (!(attackerEntity instanceof Player)) {
            Debug.log("attacker is not player");
            return;
        }
        Player victim = ((Player) victimEntity);
        Player attacker = ((Player) attackerEntity);

        Debug.log("isHider: " + isHider(victim) + " isSeeker: " + isSeeker(attacker));

        if (isHider(victim) && isSeeker(attacker)) {
            Debug.log("is hider and is seeker");
            convertToSeeker(victim, attacker);
        }
    }

    private void makeHider(Player player) {
        if (hiders.contains(player))
            return;
        hiders.add(player);
        player.showTitle(Title.title(Component.text("You are a hider!"), Component.text("")));
        notifyPlayer(player, "You have been given {grace-period-seconds} seconds grace period! Hide!"
                .replace("{grace-period-seconds}", "" + HIDER_GRACE_PERIOD_SECONDS));
    }

    public void makeSeeker(Player player) {
        if (seekers.contains(player))
            return;
        seekers.add(player);
        player.showTitle(Title.title(Component.text("You are a seeker!"), Component.text("")));
        notifyPlayer(player, "Attack other hiders in order to convert them to seekers!");
    }

    @Override
    public int getMinPlayers() {
        return 2; /// 2 for now. will be 3 later
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String getId() {
        return "kakurenbo";
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
