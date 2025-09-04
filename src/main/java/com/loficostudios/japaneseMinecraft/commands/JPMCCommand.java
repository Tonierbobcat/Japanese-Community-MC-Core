package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

public class JPMCCommand implements CommandExecutor, TabCompleter {
    private final JapaneseMinecraft plugin;

    public JPMCCommand(JapaneseMinecraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            if (args[0].equals("suggest")) {
                if (sender instanceof Player player) {
                    var suggestion = String.join(" ", args).substring(8).trim();
                    if (suggestion.isEmpty()) {
                        var message = Messages.getMessage(player, "cannot_submit_empty_suggestion");
                        player.sendMessage(Component.text(message));
                        return true;
                    }

                    suggest(player, suggestion);
                    return true;
                } else {
                    sender.sendMessage("This command can only be used by players.");
                    return true;
                }
            } else {
                sender.sendMessage("Unknown Command.");
                return true;
            }
        } else {
            sender.sendMessage("Usage: /jpmc suggest <your suggestion>");
            return true;
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("suggest");
        }
        return Collections.emptyList();
    }

    private void suggest(Player sender, String suggestion) {
        var file = new File(plugin.getDataFolder(), "suggestions/" + System.currentTimeMillis() +"-" + sender.getName() + ".txt");
        file.getParentFile().mkdirs();

        try {
            if (file.createNewFile()) {
                Files.writeString(file.toPath(), suggestion);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            var message = Messages.getMessage(sender, "failed_to_submit_suggestion");
            sender.sendMessage(Component.text(message));
            return;
        }

        var message = Messages.getMessage(sender, "successfully_submitted_suggestion");
        sender.sendMessage(Component.text(message));
    }
}
