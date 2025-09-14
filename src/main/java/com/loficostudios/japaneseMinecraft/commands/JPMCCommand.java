package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.forgified.paper.items.JItem;
import com.loficostudios.japaneseMinecraft.*;
import com.loficostudios.japaneseMinecraft.util.JishoAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JPMCCommand implements CommandExecutor, TabCompleter {
    private final JapaneseMinecraft plugin;

    public JPMCCommand(JapaneseMinecraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length > 1) {
            switch (args[0]) {
                case "suggest" -> {
                    var suggestion = String.join(" ", args).substring(8).trim();
                    if (suggestion.isEmpty()) {
                        var message = Messages.getMessage(sender, "cannot_submit_empty_suggestion");
                        sender.sendMessage(Component.text(message));
                        return true;
                    }

                    suggest(sender, suggestion);
                    return true;
                }
                case "lookup" -> {
                    lookup(sender, String.join(" ", args).substring(7).trim());
                    return true;
                }
                case "lang" -> {
                    if (args.length != 2 || (!args[1].equals("en") && !args[1].equals("jp"))) {
                        sender.sendMessage("Usage: /jpmc lang <en|jp>");
                        return true;
                    }
                    var isJapanese = args[1].equals("jp");
                    lang(sender, isJapanese);
                    return true;
                }
                case "items" -> {
                    if (!hasPermission(sender)) {
                        sender.sendMessage("You do not have permission to use this command.");
                        return true;
                    }

                    if (args.length != 2) {
                        sender.sendMessage("Usage: /jpmc items <id>");
                        return true;
                    }

                    var id = args[1];

                    JItem item = Items.ITEMS.getById(id);
                    if (item == null) {
                        sender.sendMessage("Item not found.");
                        return true;
                    }

                    sender.getInventory().addItem(Items.ITEMS.createItemStack(item));
                    return true;
                }
                case "start-shiritori" -> {
                    if (!hasPermission(sender)) {
                        sender.sendMessage("You do not have permission to use this command.");
                        return true;
                    }
                    if (plugin.getShiritoriManager().tryStartNewGame()) {
                        sender.sendMessage("Started shiritori");
                    } else {
                        sender.sendMessage("Could not start shiritori. too few players or there is already a game running");
                    }
                    return true;
                }
                case "post-bounty" -> {
                    if (!hasPermission(sender)) {
                        sender.sendMessage("You do not have permission to use this command.");
                        return true;
                    }
                    var message = args[1];
                    var reward = args[2];

                    /// run async because of file io
                    var file = new File(plugin.getDataFolder(), "bounties.yaml");
                    JapaneseMinecraft.runTaskAsynchronously(() -> {
                        new BountyService(file)
                                .post(new BountyService.Bounty(message, Double.parseDouble(reward)));
                        sender.sendMessage("Successfully posted bounty");
                    });

                    return true;
                }
                default -> {
                    sender.sendMessage("Unknown Command.");
                    return true;
                }
            }
        } else {
            sender.sendMessage("Invalid command");
            return true;
        }
    }

    private boolean hasPermission(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public @NotNull List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            var commands = new ArrayList<>(List.of("suggest", "lookup", "lang", "items"));
            if (hasPermission(sender)) {
                commands.addAll(List.of("start-shiritori", "post-bounty"));
            }
            return commands;
        } else if (args.length == 2) {
            if (args[0].equals("lang")) {
                return List.of("en", "jp");
            } else if (args[0].equals("items")) {
                if (!hasPermission(sender)) {
                    return Collections.emptyList();
                }
                return Items.ITEMS.getRegistered().stream().map(JItem::getId).toList();
            }
        }
        return Collections.emptyList();
    }

    private void lookup(Player sender, String query) {
        var jisho = new JishoAPI();
        if (query.isEmpty()) {
            sender.sendMessage("Please provide a word to look up.");
            return;
        }
        CompletableFuture.supplyAsync(() -> jisho.getFirstSearchResultSimple(query)).thenAccept((result) -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                var isEmpty = result == null;
                if (isEmpty) {
                    var eng = "No results found for '" + query + "'.";
                    var jp = "'" + query + "'の結果が見つかりません。";
                    sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
                    return;
                }
                sender.sendMessage(Common.getDictionaryMessageFromResult(result));
            });
        });
    }

    private void lang(Player sender, boolean isJapanese) {
        JapaneseMinecraft.getPlayerProfile(sender)
                .setLanguage(isJapanese ? Language.JAPANESE : Language.ENGLISH);
        var eng = "Your language has been set to English.";
        var jp = "あなたの言語は日本語に設定されました。";
        sender.sendMessage(isJapanese ? jp : eng);
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
