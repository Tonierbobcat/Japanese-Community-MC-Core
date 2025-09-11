package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.Messages;
import com.loficostudios.japaneseMinecraft.util.NoteBlockAPIWrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/// This command allows the player to play music and to create playlists
/// for now we can only play and stop songs
public class SpicifyCommand implements CommandExecutor, TabCompleter {
    private final JapaneseMinecraft plugin;
    private static final String PREFIX = Common.createMessagePrefix("Spicify", "§6");

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
                List<String> strings = new ArrayList<>();
                for (int i = 1; i < args.length; i++) {
                    if (i > args.length - 1)
                        continue;
                    strings.add(args[i]);
                }

                var key = String.join(" ", strings);

                if (key.isEmpty()){
                    /// Send key is empty text rather than invalid key
                    sender.sendMessage(PREFIX + Messages.getMessage(sender, "must_enter_valid_song_id")
                            .replace("{key}", key));
                    return true;
                }

                try {
                    musicWrapper.playSong(key, sender);
                } catch (Exception ignore) {
                    sender.sendMessage(PREFIX + Messages.getMessage(sender, "must_enter_valid_song_id")
                            .replace("{key}", key));

                    /// SEND HINT OF LIBRARY LIST
                    // todo move this to messages
                    var eng = "Use '/spicify list' to view a list of our library!";
                    sender.sendMessage(PREFIX + eng);
                    return true;
                }
                sender.sendMessage(PREFIX + Messages.getMessage(sender, "now_playing").replace("{song}", Common.formatEnumName(key)));
            }
            case "stop" -> {
                var wasListening = NoteBlockAPIWrapper.isListening(sender);
                musicWrapper.stopSong(sender);

                sender.sendMessage(PREFIX + (wasListening
                        ? Messages.getMessage(sender, "stopped_listening")
                        : Messages.getMessage(sender, "not_listening_to_anything")));
            }
            case "list" -> {
                var strings = getSongKeys();
                sender.sendMessage(String.join("\n", strings));
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

    /// We are not storing songs on the repo because of copyright infringements
    /// instead we get the songs locally by iterating through the songs folder
    private List<String> getSongKeys() {
        var songsFolder = new File(plugin.getDataFolder(), "songs");
        songsFolder.mkdirs();
        var result = new ArrayList<String>();
        for (File file : songsFolder.listFiles()) {
            var name = file.getName().replace(".nbs", "");
            result.add(name);
        }
        return result;
    }
}
