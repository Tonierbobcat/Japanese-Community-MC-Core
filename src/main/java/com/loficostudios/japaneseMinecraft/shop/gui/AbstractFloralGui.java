/**
 * @Author Tonierbobcat
 * @Github https://github.com/Tonierbobcat
 * @Link https://github.com/Tonierbobcat/MelodyAPI
 * @version 0.1.3
 */



package com.loficostudios.japaneseMinecraft.shop.gui;


import com.loficostudios.japaneseMinecraft.shop.gui.events.GuiOpenEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFloralGui implements FloralGui {
    protected static final String DEFAULT_MENU_TITLE = "&#C608FBM&#C322FBe&#C03CFBl&#BE56FCo&#BB70FCd&#B88BFCy &#B3BFFDG&#B0D9FDU&#ADF3FDI";

    private Inventory inventory;
    private final Map<Integer, GuiIcon> displayedIcons = new HashMap<>();

//    private String title;

    private Component title;

    private final int size;

    public AbstractFloralGui(int size) {
        this(size, null);
    }

    public AbstractFloralGui(int size, Component title) {
        this.size = FloralGui.validateSize(size);
        this.title = title;
        this.inventory = Bukkit.createInventory(this,
                this.size,
                title != null ? title : Component.text(DEFAULT_MENU_TITLE));
    }

    public boolean open(@NotNull Player player) {
        try {
            create(player);
        } catch (Exception e) {
            e.printStackTrace();
        }

        var event = new GuiOpenEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        player.openInventory(this.inventory);
        return true;
    }

    public boolean close(@NotNull Player player) {
        player.closeInventory();
        return true;
    }

    public void refresh() {
    }

    public Collection<GuiIcon> getDisplayedIcons() {
        return new ArrayList<>(this.displayedIcons.values());
    }

    public GuiIcon getIcon(int slot) {
        return this.displayedIcons.get(slot);
    }

    public int getSize() {
        return this.size;
    }

    public @NotNull Component getTitle() {
        return this.title;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean setSlot(int slot, @Nullable ItemStack item) {
        if (item == null) {
            setSlot(slot, new GuiIcon(ItemStack.of(Material.AIR), ""));
            return true;
        }

        setSlot(slot, new GuiIcon(item, item.getType().name().toLowerCase() + System.currentTimeMillis()));
        return true;
    }

    @Override
    public boolean setSlot(int slot, @Nullable GuiIcon icon) {
        if (icon == null) {
            setSlot(slot, new GuiIcon(ItemStack.of(Material.AIR), ""));
            return true;
        }
        this.displayedIcons.put(slot, icon);
        this.inventory.setItem(slot, icon.item());
        return true;
    }

    public void setTitle(@NotNull Component text) {
        this.title = text;

        var contents = inventory.getContents();
        var viewers = new ArrayList<>(inventory.getViewers());
        for (HumanEntity viewer : viewers) {
            viewer.closeInventory();
        }
        inventory = Bukkit.createInventory(this, this.size, this.title);
        for (HumanEntity viewer : viewers) {
            viewer.openInventory(this.inventory);
        }
        inventory.setContents(contents);
    }

    protected void fill(@NotNull GuiIcon icon, int start, int end, Boolean replaceExisting) {
        for(int i = start; i < end; ++i) {

            if (!replaceExisting && this.displayedIcons.containsKey(i)) {
                continue; // Skip this iteration if replaceExisting is false and key exists
            }

            setSlot(i, icon);
        }
    }

    protected void clear() {
        if (displayedIcons.isEmpty()) return;

        var entries = new ArrayList<>(displayedIcons.entrySet());

        for (Map.Entry<Integer, GuiIcon> entry : entries) {
            this.getInventory().setItem(entry.getKey(), new ItemStack(Material.AIR));
        }


        displayedIcons.clear();
    }

    protected void clear(GuiIcon... excluded) {
        if (displayedIcons.isEmpty()) return;

        var entries = new ArrayList<>(displayedIcons.entrySet());
        for (Map.Entry<Integer, GuiIcon> entry : entries) {
            var found = false;
            for (GuiIcon guiIcon : excluded) {
                if (entry.getValue() .equals(guiIcon)) {
                    found = true;
                    break;
                }
            }
            if (found)
                this.getInventory().setItem(entry.getKey(), new ItemStack(Material.AIR));
        }

        displayedIcons.clear();
    }
}



