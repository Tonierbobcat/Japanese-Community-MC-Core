package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.Messages;
import com.loficostudios.japaneseMinecraft.spicify.SpicifyService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/// This command allows the player to play music and to create playlists
/// for now we can only play and stop songs
/// Alot of the code here should be moved to a service. maybe SpicifyService.class
public class SpicifyCommand implements CommandExecutor, TabCompleter {
    private final JapaneseMinecraft plugin;

    private final SpicifyService service;

    public SpicifyCommand(JapaneseMinecraft plugin) {
        this.plugin = plugin;
        this.service = new SpicifyService(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("You must be a player!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("Usage: spicify play <song>, spicify stop");
            return true;
        }

        switch (args[0]) {
            case "play" -> {
                List<String> strings = new ArrayList<>();
                for (int i = 1; i < args.length; i++) {
                    if (i > args.length - 1)
                        continue;
                    strings.add(args[i]);
                }

                var key = String.join(" ", strings);

                if (key.isEmpty()){
                    /// Send title is empty text rather than invalid title
                    sender.sendMessage(SpicifyService.PREFIX + Messages.getMessage(sender, "must_enter_valid_song_id")
                            .replace("{title}", key));
                    return true;
                }

                try {
                    service.playSong(sender, key);

                } catch (Exception ignore) {
                    sender.sendMessage(SpicifyService.PREFIX + Messages.getMessage(sender, "must_enter_valid_song_id")
                            .replace("{title}", key));

                    // move to messages
                    var eng = "Use '/spicify list' to view a list of our library!";
                    sender.sendMessage(SpicifyService.PREFIX + eng);
                    return true;
                }
                sender.sendMessage(SpicifyService.PREFIX + Messages.getMessage(sender, "now_playing").replace("{song}", key));
            }
            case "stop" -> {
                var wasListening = service.isListening(sender);
                service.stopSong(sender);

                sender.sendMessage(SpicifyService.PREFIX + (wasListening
                        ? Messages.getMessage(sender, "stopped_listening")
                        : Messages.getMessage(sender, "not_listening_to_anything")));
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

                var strings = service.getSongKeys();
                sender.sendMessage(service.getPage(strings, page));
            }
            case "current" -> {
                var song = service.getCurrentSong(sender);
                if (song == null) {
                    sender.sendMessage(SpicifyService.PREFIX + "You are currently not listening to anything");
                    return true;
                }
                sender.sendMessage(SpicifyService.PREFIX + "Listening to {current} §l{likes} §f[§c❤§f]"
                                .replace("{likes}", "" + song.likes())
                        .replace("{current}", song.title()));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return List.of("play", "stop", "list");
        }
        return List.of();
    }
}
