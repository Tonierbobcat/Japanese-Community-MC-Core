/**
 * @Author Tonierbobcat
 * @Github https://github.com/Tonierbobcat
 * @Link https://github.com/Tonierbobcat/MelodyAPI
 * @version 0.1.3
 */

package com.loficostudios.japaneseMinecraft.shop.gui;


import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.cooldown.Cooldown;
import com.loficostudios.japaneseMinecraft.cooldown.SimpleCooldown;
import com.loficostudios.japaneseMinecraft.shop.gui.events.GuiCloseEvent;
import com.loficostudios.japaneseMinecraft.shop.gui.events.GuiIconClickEvent;
import com.loficostudios.japaneseMinecraft.shop.gui.events.GuiOpenEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GuiManager implements Listener {

    private final Map<UUID, FloralGui> openedMenus = new HashMap<>();
    private final Cooldown cooldowns = new SimpleCooldown(250);

    public FloralGui getGui(@NotNull Player player) {
        return this.openedMenus.get(player.getUniqueId());
    }

    public void setGui(@NotNull Player player, @Nullable FloralGui gui) {
        UUID uuid = player.getUniqueId();
        if (gui == null && openedMenus.containsKey(uuid))
            this.openedMenus.remove(uuid);
        this.openedMenus.put(uuid, gui);
    }

    @EventHandler
    protected void onClick(InventoryClickEvent e) {
        if (e.isCancelled())
            return;
        if (!(e.getInventory().getHolder() instanceof FloralGui gui))
            return;
        Player player = (Player) e.getWhoClicked();

//        IGui gui = getGui(player);
//        if (gui == null) {
//            Debug.log("Gui is null");
//            return;
//        }

        var slot = e.getRawSlot();

        if (gui instanceof MutableGui) {
            handleMutableGui(e, ((MutableGui) gui));
            return;
        }
        e.setCancelled(true);
        handleClick(e, player, gui, slot);
    }

    private void handleMutableGui(InventoryClickEvent e, MutableGui gui) {
        Player player = (Player) e.getWhoClicked();

        var slot = e.getRawSlot();

        if (gui.getMutableSlots().contains(slot)) {
//                Debug.log("clicked on item is mutable");
            var action = e.getAction();
            if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                e.setCancelled(true);
//                    var contents = e.getInventory().getContents();
//                    Integer targetSlot = null;
//                    for (int i = 0; i < contents.length; i++) {
//                        if (contents[i] == null || contents[i].getType().equals(Material.AIR)) {
//                            targetSlot = i;
//                            break;
//                        }
//                    }
//                    if (!((MutableGui) gui).getMutableSlots().contains(targetSlot))
//                        e.setCancelled(true);
            }
            return;
        }
        e.setCancelled(true);

//            Debug.log("clicked on item is not mutable");

        handleClick(e, player, gui, slot);
    }

    private void handleClick(InventoryClickEvent e, Player player, FloralGui gui, int slot) {
        var icon = gui.getIcon(slot);

        if (icon == null)
            return;

        if (cooldowns.has(player.getUniqueId()))
            return;

        var event = new GuiIconClickEvent(player, gui, icon);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            icon.onClick(e);
        }
    }

    private final Set<Player> transitioningPlayers = new HashSet<>();

    @EventHandler
    private void onOpen(GuiOpenEvent e) {
        var player = e.getPlayer();
        setGui(player, e.getGui());

        transitioningPlayers.add(player);
        JapaneseMinecraft.runTaskLater(() -> transitioningPlayers.remove(player), 2L);
    }

    @EventHandler
    private void onClose(GuiCloseEvent e) {
        var player = e.getPlayer();
        setGui(player, null);
        if ((e.getGui() instanceof PopOutGui gui))
            handlePopOutGui(e, gui);
    }

    private void handlePopOutGui(GuiCloseEvent e, PopOutGui gui) {
        JapaneseMinecraft.runTaskLater(() -> gui.onClose(e.getPlayer()), 1);
    }
    public boolean isTransitioning(Player player) {
        return transitioningPlayers.contains(player);
    }
    @EventHandler
    private void onClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof FloralGui gui))
            return;
        if (!(e.getPlayer() instanceof Player player))
            return;
        if (isTransitioning(player)) {
            return;
        }

        var event = new GuiCloseEvent((player), gui);
        Bukkit.getPluginManager().callEvent(event);
    }
}