package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeCommand implements CommandExecutor {
    private final Map<UUID, Long> confirmations = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("");
            return true;
        }
        var eng = "Teleporting home...";
        var jp = "ホームにテレポートしています...";
        sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
        sender.teleport(sender.getRespawnLocation() != null ? sender.getRespawnLocation() : sender.getWorld().getSpawnLocation());




        /// Add this back later
//        /// Use currency as a teleportation fee;
//        /// For we are using player levels as a currency
//        if (sender.getLevel() < 1) {
//            var eng = "You need at least 1 level to teleport home.";
//            var jp = "ホームにテレポートするには、少なくとも1レベルが必要です。";
//            sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
//            return true;
//        } else {
//            // I need to make this query expire after X amount of time
//            var when = confirmations.get(sender.getUniqueId());
//            final long expiry = 5*1000; // 5 seconds
//            if (when != null && System.currentTimeMillis() - when > expiry) {
//                confirmations.remove(sender.getUniqueId());
//            }
//            var existing = confirmations.containsKey(sender.getUniqueId());
//            if (existing) {
//                var eng = "Teleportation confirmed. Teleporting home...";
//                var jp = "テレポートが確認されました。ホームにテレポートしています...";
//                sender.setLevel(sender.getLevel() - 1);
//                sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
//                sender.teleport(sender.getRespawnLocation() != null ? sender.getRespawnLocation() : sender.getWorld().getSpawnLocation());
//                return true;
//            }
//            var eng = "Are you sure you want to teleport home? This will cost 1 level. Type /jpmc home again to confirm.";
//            var jp = "ホームにテレポートしてもよろしいですか？ これには1レベルが必要です。確認するにはもう一度/jpmc homeと入力してください。";
//            sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
//        }
        return true;
    }
}
