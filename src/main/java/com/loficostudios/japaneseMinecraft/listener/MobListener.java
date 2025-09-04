package com.loficostudios.japaneseMinecraft.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class MobListener implements Listener {
    @EventHandler
    private void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().clear();
    }

    @EventHandler
    private void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (e.getEntity() instanceof Player) {
            return;
        }
        e.setCancelled(true);
    }
}
