package com.loficostudios.japaneseMinecraft.games;


import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/// Hide and seek
public class KakurenboGame implements Game, Listener {

    public static final String PREFIX = Common.createMessagePrefix("Kakurenbo", "§9");

    private boolean active;

    @Override
    public int getLengthMinutes() {
        return 1;
    }

    private List<Player> seekers = new ArrayList<>();
    private List<Player> hiders = new ArrayList<>();
    private int hidersAtStart;
    private BukkitTask task;
    @Override
    public void reset() {
        hidersAtStart = 0;
        hiders.clear();
        seekers.clear();
    }

    /// This is only called when the game timesout
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
    /// [The length of the game is the length of this song](https://musescore.com/user/54937749/scores/11525008)
    @Override
    public void start() {
        active = true;
        int maxSeekers = 2;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            notifyPlayer(onlinePlayer, """
                    ---------- {prefix} ------------
                    In this game players are split into two teams
                    hiders & seekers. both teams get equal rewards
                    the top 3 seekers with the most catches gets rewards
                    hiders share a $1000 per player hider reward pool
                    hiders have {grace-period-seconds}s to hide
                    -------------------------------
                    """.replace("{prefix}", PREFIX).replace("{grace-period-seconds}", "" + 30));
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (seekers.size() <= maxSeekers) {
                var rand = ThreadLocalRandom.current().nextDouble();
                var isSeeker = rand > 0.5;
                if (isSeeker)
                    makeSeeker(player);
                else
                    makeHider(player);
            } else {
                makeHider(player);
            }
        }
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

    enum KakurenboStopReason {
        TIMEOUT,
        NO_HIDERS
    }

    @EventHandler
    private void onTag(EntityDamageByEntityEvent e) {
        var victimEntity = e.getEntity();
        var attackerEntity  =e.getDamager();
        if (victimEntity instanceof Player hider && attackerEntity instanceof Player seeker) {
            if (isSeeker(seeker) && isHider(hider)) {
                convertToSeeker(hider, seeker);
            }
        }
    }

    private void makeHider(Player player) {
        notifyPlayer(player, "You are a hider. Hide!");
    }

    public void makeSeeker(Player player) {
        seekers.add(player);
        notifyPlayer(player, "You are a seeker look for players and hit them with your hand");
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
