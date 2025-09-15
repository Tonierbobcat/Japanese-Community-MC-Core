package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.config.Shops;
import com.loficostudios.japaneseMinecraft.shop.CostModifier;
import com.loficostudios.japaneseMinecraft.shop.Operation;
import com.loficostudios.japaneseMinecraft.shop.ShopInstance;
import com.loficostudios.japaneseMinecraft.shop.gui.ShopGui;
import com.loficostudios.japaneseMinecraft.shop.gui.ShopGuiTemplate;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopCommand implements CommandExecutor {
    public ShopCommand() {
        for (ShopInstance<VanillaShopItem> instance : Shops.BUILDER_SHOP.getInstances()) {
            instance.removeModifier("builder-sale");
            instance.addModifier(new CostModifier("builder-sale", 0.5, Operation.MULTIPLY));
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("Only players can use this!!");
            return true;
        }

        new ShopGui<>(Shops.BUILDER_SHOP, ShopGuiTemplate.generic(Component.text("Shop")), JapaneseMinecraft::getPlayerProfile)
                .open(sender);
        return true;
    }
}