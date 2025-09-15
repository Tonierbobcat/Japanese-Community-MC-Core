package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.forgified.paper.gui.GuiIcon;
import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;
import com.loficostudios.japaneseMinecraft.shop.ShopInstance;
import com.loficostudios.japaneseMinecraft.shop.ShopItem;
import com.loficostudios.japaneseMinecraft.shop.ShopTransactionResult;
import com.loficostudios.japaneseMinecraft.shop.Stackable;
import com.loficostudios.japaneseMinecraft.shop.gui.ShopGuiIcon;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public class VanillaShopItem implements ShopItem<VanillaShopItem>, Stackable, ShopGuiIcon<VanillaShopItem> {
    private final Material material;

    public VanillaShopItem(Material material) {
        this.material = material;
    }

    @Override
    public ShopTransactionResult<VanillaShopItem> onBuy(ShopInstance<VanillaShopItem> instance, int amount, PlayerProfile profile) {
        var player = profile.getPlayer();
        if (player == null) {
            Debug.logError("player is not online!!");
            return new ShopTransactionResult<>(instance, amount, ShopTransactionResult.Type.FAILURE);
        }
        var stack = new ItemStack(material, Math.max(amount, 1));
        player.getInventory().addItem(stack);
        return new ShopTransactionResult<>(instance, amount, ShopTransactionResult.Type.SUCCESS);
    }

    @Override
    public String getName() {
        return Common.formatEnumName(material);
    }

    @Override
    public GuiIcon getIcon(ShopInstance<VanillaShopItem> instance, int amount, BiConsumer<Player, ClickType> onClick) {
        var icon = new GuiIcon(instance.getItem().material, Component.text(instance.getItem().getName()));
        icon.onClick(onClick);
        return icon;
    }
}
