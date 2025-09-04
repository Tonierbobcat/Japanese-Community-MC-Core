package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.japaneseMinecraft.ChatManager;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DMCommand implements CommandExecutor, TabCompleter {
    private final ChatManager chatManager;

    public DMCommand(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length != 1) {
            var dm = chatManager.getDM(sender);
            if (chatManager.stopDM(sender)) {
                assert dm != null;
                var eng = "You have stopped messaging " + dm + ".";
                var jp = dm + "へのDMを終了しました。";
                sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
            } else {
                var eng = "You are not currently not messaging anyone.";
                var jp = "現在、DMを送っている相手はいません。";
                sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
            }

            return true;
        }


        var targetPlayer = sender.getServer().getPlayerExact(args[0]);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            var eng = "Player not found or not online.";
            var jp = "プレイヤーが見つからないか、オンラインではありません。";
            sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
            return true;
        }

        if (targetPlayer.getUniqueId().equals(sender.getUniqueId())) {
            var eng = "You cannot DM yourself.";
            var jp = "自分自身にDMを送ることはできません。";
            sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
            return true;
        }

        chatManager.startDM(sender, targetPlayer);

        var eng = "You are now in a direct messaging " + targetPlayer.getName() + ". Type /dm to stop.";
        var jp = targetPlayer.getName() + "にDMを送っています。終了するには/dmと入力してください。";
        sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        var players = commandSender.getServer().getOnlinePlayers();
        if (strings.length == 1) {
            return players.stream().map(Player::getName).toList();
        }
        return null;
    }
}
