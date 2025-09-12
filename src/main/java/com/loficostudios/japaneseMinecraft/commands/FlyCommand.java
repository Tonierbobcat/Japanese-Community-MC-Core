package com.loficostudios.japaneseMinecraft.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("Only players can use this command!");
            return true;
        }

        var isFlying = sender.isFlying();
        if (isFlying) {
            sender.setAllowFlight(false);
            sender.setFlying(false);
            sender.sendMessage("You are no longer flying!");
        } else {
            sender.setAllowFlight(true);
            sender.sendMessage("You can now fly!");
        }

        return true;
    }
}
