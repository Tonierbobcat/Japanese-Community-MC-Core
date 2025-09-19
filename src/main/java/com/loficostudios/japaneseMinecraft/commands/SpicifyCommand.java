package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.Messages;
import com.loficostudios.japaneseMinecraft.service.spicify.SpicifyService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/// This command allows the player to play music and to create playlists
/// for now we can only play and stop songs
/// Alot of the code here should be moved to a service. maybe SpicifyService.class
public class SpicifyCommand implements CommandExecutor, TabCompleter {

    private final SpicifyService service;

    public SpicifyCommand(JapaneseMinecraft plugin) {
        this.service = new SpicifyService(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        String usageMessage = "Usage: /spicify play <id> | stop | list <page> | current | search <query>";
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("You must be a player!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(usageMessage);
            return true;
        }

        switch (args[0]) {
            case "play" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /spicify play <id>");
                    return true;
                }

                int id = -1;

                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(SpicifyService.PREFIX + Messages.getMessage(sender, "must_enter_valid_song_id")
                            .replace("{title}", "" + id));
                    return true;
                }
                var song = service.getSong(id);


                if (song == null){
                    sender.sendMessage(SpicifyService.PREFIX + Messages.getMessage(sender, "must_enter_valid_song_id")
                            .replace("{title}", "" + id));
                    return true;
                }

                var playing = service.playSong(sender, song);
                if (!playing) {
                    sender.sendMessage(SpicifyService.PREFIX + Messages.getMessage(sender, "must_enter_valid_song_id")
                            .replace("{title}", "" + id));

                    // move to messages
                    var eng = "Use '/spicify list' to view a list of our library!";
                    sender.sendMessage(SpicifyService.PREFIX + eng);
                    return true;
                }
                sender.sendMessage(SpicifyService.PREFIX + Messages.getMessage(sender, "now_playing").replace("{song}", song.title()));
                return true;
            }
            case "stop" -> {
                var wasListening = service.isListening(sender);
                service.stopSong(sender);

                sender.sendMessage(SpicifyService.PREFIX + (wasListening
                        ? Messages.getMessage(sender, "stopped_listening")
                        : Messages.getMessage(sender, "not_listening_to_anything")));
                return true;
            }
            case "list" -> {
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                /// Clamp page
                page = Math.max(1, page);

                sender.sendMessage(service.getPage(service.getAll(), page));
                return true;
            }
            case "current" -> {
                var song = service.getCurrentSong(sender);
                if (song == null) {
                    sender.sendMessage(SpicifyService.PREFIX + "You are currently not listening to anything");
                    return true;
                }
                sender.sendMessage(SpicifyService.PREFIX + "Listening to {current} §f[§c❤§f §l{likes}§f]"
                                .replace("{likes}", "" + song.likes())
                        .replace("{current}", song.title()));
                return true;
            }
            case "search" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /spicify search <query>");
                    return true;
                }
                String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                /// run this async
                JapaneseMinecraft.runTaskAsynchronously(() -> {
                    var results = service.search(query);
                    JapaneseMinecraft.runTask(() -> {
                        /// I only really expect one page of results
                        sender.sendMessage(service.getPage(results, 1));
                    });
                });
                return true;
            }
            default -> {
                sender.sendMessage(usageMessage);
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("play", "stop", "list", "current", "search");
        }
        return List.of();
    }
}
