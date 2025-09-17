package com.loficostudios.japaneseMinecraft.listener;

import com.loficostudios.japaneseMinecraft.Items;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.config.Shops;
import com.loficostudios.japaneseMinecraft.shop.gui.ShopGui;
import com.loficostudios.japaneseMinecraft.shop.gui.ShopGuiTemplate;
import com.loficostudios.townsplugin.api.BlockLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemListener implements Listener {
    private static ItemListener instance;

    public ItemListener() {
        instance = this;
    }

    private final Map<UUID, Location> lightSources = new HashMap<>();


    @EventHandler
    private void onInteract(PlayerInteractEvent e) {
        var item = e.getItem();
        var player = e.getPlayer();
        if (Items.isItem(item, Items.TELEPORT_CRYSTAL)) {
            e.setCancelled(true);
            handleTeleportCrystal(player);
        } else if (Items.isItem(item, Items.BASIC_FLASHLIGHT)) {
            e.setCancelled(true);
        } else if (Items.isItem(item, Items.LEVEL_CANDY)) {
            e.setCancelled(true);
        } else if (Items.isItem(item, Items.BUILDERS_CHEST)) {
            e.setCancelled(true);
            new ShopGui<>(Shops.BUILDER_SHOP, ShopGuiTemplate.generic(Component.text("Shop")), JapaneseMinecraft::getPlayerProfile)
                    .open(player);
        } else if (Items.isItem(item, Items.REGION_STICK)) {
            e.setCancelled(true);
            var action = e.getAction();
            var clickedBlock = e.getClickedBlock();
            if (clickedBlock == null)
                return;
            var api =  JapaneseMinecraft.getTownsAPI();
            var prefix = api.getAPIConfig().getPrefix();

            switch (action) {
                case LEFT_CLICK_BLOCK -> {
                    var min = BlockLocation.from(clickedBlock);
                    JapaneseMinecraft.getTownsAPI().getPlayerSelectionManager().setMin(player, min);
                    player.sendMessage(prefix + "Set first position to {x},{y},{z}"
                            .replace("{x}", "" + min.getX())
                            .replace("{y}", "" + min.getY())
                            .replace("{z}", "" + min.getZ()));
                }
                case RIGHT_CLICK_BLOCK -> {
                    var max = BlockLocation.from(clickedBlock);
                    JapaneseMinecraft.getTownsAPI().getPlayerSelectionManager().setMax(player, max);
                    player.sendMessage(prefix + "Set second position to {x},{y},{z}"
                            .replace("{x}", "" + max.getX())
                            .replace("{y}", "" + max.getY())
                            .replace("{z}", "" + max.getZ()));
                }
            }
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        var itemInMainHand = e.getPlayer().getInventory().getItemInMainHand();
        var itemInOffHand = e.getPlayer().getInventory().getItemInOffHand();
        if (Items.isItem(itemInMainHand, Items.BASIC_FLASHLIGHT) || Items.isItem(itemInOffHand, Items.BASIC_FLASHLIGHT)) {
            handleFlashlight(e);
        } else {
            removeLastLightSource(e.getPlayer());
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        removeLastLightSource(e.getPlayer());
    }

    private void handleTeleportCrystal(Player sender) {
        var eng = "Teleporting home...";
        var jp = "ホームにテレポートしています...";
        sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
        sender.teleport(sender.getRespawnLocation() != null ? sender.getRespawnLocation() : sender.getWorld().getSpawnLocation());
    }

    private void handleFlashlight(PlayerMoveEvent e) {
        var uuid = e.getPlayer().getUniqueId();

        var level = 15;

        var loc = e.getTo();
        removeLastLightSource(e.getPlayer());
        var block = loc.getBlock();
        if (!block.getType().equals(Material.AIR))
            return;
        block.setType(Material.LIGHT);
        Light light = (Light) block.getBlockData();
        light.setLevel(level);
        block.setBlockData(light);
        lightSources.put(uuid, loc);
    }

    private void removeLastLightSource(Player player) {
        removeLastLightSource(player.getUniqueId());
    }

    private void removeLastLightSource(UUID uuid) {
        var last = lightSources.remove(uuid);
        if (last != null) {
            var block = last.getBlock();
            if (block.getType().equals(Material.LIGHT))
                block.setType(Material.AIR);
        }
    }

    public static void clearLightSources() {
        if (instance == null)
            return;
        for (UUID uuid : instance.lightSources.keySet()) {
            instance.removeLastLightSource(uuid);
        }
    }
}
