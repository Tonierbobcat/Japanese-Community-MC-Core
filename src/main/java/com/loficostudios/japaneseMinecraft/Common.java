package com.loficostudios.japaneseMinecraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;

public class Common {
    public static void notifyPlayers(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    public static void notify(Player player, String message) {
        player.sendMessage(message);
    }

    public static void notifyAdmins(String message) {
        Bukkit.getOnlinePlayers().stream()
                .filter(ServerOperator::isOp)
                .forEach(p -> p.sendMessage(message));
    }
}
