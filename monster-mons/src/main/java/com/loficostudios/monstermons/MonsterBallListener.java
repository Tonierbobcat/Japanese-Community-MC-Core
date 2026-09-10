package com.loficostudios.monstermons;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

// TODO move some functionality to seperate classes
// TODO ADD TOP LEVEL DOCS
/// This class is the manager for monsterballs
public class MonsterBallListener implements Listener { //todo maybe?? rename to somethingManager

    private final MonsterBallManager monsterBallManager;

    public MonsterBallListener(MonsterMonsPlugin plugin) {
        this.monsterBallManager =  new MonsterBallManager(plugin);
    }

    @EventHandler
    private void onThrow(ProjectileLaunchEvent e) {
        var projectile = e.getEntity();
        if (!(projectile instanceof Snowball))
            return;
        var shooter = e.getEntity().getShooter();
        if (!(shooter instanceof Player player))
            return;
        monsterBallManager.handleThrow(player, projectile);
    }

    @EventHandler
    private void onSpawn(EntitySpawnEvent e) {
//        monsterBallManager.handleSpawn(e.getEntity());
    }

    @EventHandler
    private void onHit(ProjectileHitEvent e) {
        monsterBallManager.handleBallHit(e.getEntity(), e.getHitEntity());
    }

    @EventHandler
    private void onInteract(PlayerInteractAtEntityEvent e) {
        monsterBallManager.handleEntityInteraction(e.getPlayer(), e.getRightClicked(), e.getHand());
    }

    /// lowest here so that it can be cancelled
    @EventHandler(priority = EventPriority.LOWEST)
    private void onInteract(PlayerInteractEvent e) {
        monsterBallManager.handleInteraction(e);
    }
}
