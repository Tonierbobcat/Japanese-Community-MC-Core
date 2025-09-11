package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.util.NoteBlockAPIWrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// This command allows the player to play music and to create playlists
/// for now we can only play and stop songs
public class SpicifyCommand implements CommandExecutor, TabCompleter {
    private final JapaneseMinecraft plugin;

    public SpicifyCommand(JapaneseMinecraft plugin) {
        this.plugin = plugin;
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

        var musicWrapper = new NoteBlockAPIWrapper(plugin);

        switch (args[0]) {
            case "play" -> {
                if (args.length > 2 || args[1].isEmpty()) {
                    sender.sendMessage("You must enter a valid song");
                }
                var key = args[1];
                musicWrapper.playSong(key, sender);
                sender.sendMessage("Now playing " + key + "...");
            }
            case "stop" -> {
                musicWrapper.stopSong(sender);
                sender.sendMessage(NoteBlockAPIWrapper.isListening(sender) ? "Stopped listening..." : "You were not listening to anything");
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return switch (args[0]) {
                case "play" -> getSongKeys();
                default -> List.of();
            };
        }
        return List.of("play", "stop");
    }

    /// We are not storing songs on the repo because of copyright infringements
    /// instead we get the songs locally by iterating through the songs folder
    private List<String> getSongKeys() {
        return List.of();
    }
}
