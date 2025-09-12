package com.loficostudios.japaneseMinecraft.commands;

import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.Messages;
import com.loficostudios.japaneseMinecraft.util.NoteBlockAPIWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/// This command allows the player to play music and to create playlists
/// for now we can only play and stop songs
/// Alot of the code here should be moved to a service. maybe SpicifyService.class
public class SpicifyCommand implements CommandExecutor, TabCompleter {
    private final JapaneseMinecraft plugin;
    private static final String COLOR_LEGACY = "§6";
    private static final String COLOR_MM = "<gold>";
    private static final String PREFIX = Common.createMessagePrefix("Spicify", COLOR_LEGACY);
    private static final int ITEMS_PER_PAGE = 8;
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
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                /// Clamp page
                page = Math.max(1, page);

                var strings = getSongKeys();
                sender.sendMessage(getPage(strings, page));
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

    /// page starts at 0
    private Component getPage(List<String> songs, int page) {
        var header = "-- {prefix} {current-page}/{max-page} --";
        var prevPage = page > 0
                ? "<click:run_command:'/spicify list " + (page - 1) + "'><yellow>« Prev</yellow></click>"
                : "<gray>« Prev</gray>";

        var nextPage = page < getMaxPage(songs) - 1
                ? "<click:run_command:'/spicify list " + (page + 1) + "'><yellow>Next »</yellow></click>"
                : "<gray>Next »</gray>";

        var footer = prevPage + " <gray>|</gray> " + nextPage;

        List<String> format = new ArrayList<>();

        List<String> paginated = new ArrayList<>(paginate(songs, (page - 1), ITEMS_PER_PAGE));

        /// Trim the prefix and replace legacy code with mm format
        format.add(header
                .replace("{prefix}", PREFIX.trim().replace(COLOR_LEGACY, COLOR_MM).replace("§8", "<gray>").replace("§r", "<reset>"))
                .replace("{current-page}", "" + page)
                .replace("{max-page}", "" + getMaxPage(songs)));

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            var line = "  - {song} [{play-icon}<reset>][<green>+<reset>][<red>-<reset>]";

            if (i > paginated.size() - 1) {
                format.add("  -");
                continue;
            }

            var song = paginated.get(i);
            var playIcon = "<click:run_command:/spicify play {song}>{spicify-color}▶</click>";

            format.add(line
                    .replace("{play-icon}", playIcon.replace("{spicify-color}", COLOR_MM))
                    .replace("{song}", song));
        }

        format.add(footer);

        return MiniMessage.miniMessage().deserialize(String.join("\n", format));
    }

    private int getMaxPage(List<String> songs) {
        if (songs == null || songs.isEmpty()) {
            return 1; // at least 1 page, even if no songs
        }
        return (songs.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
    }

    /// Util from one of my other projects
    //todo move this to util class
    static <T> Collection<T> paginate(List<T> objects, int page, int itemsPerPage) {
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, objects.size());

        if (start >= objects.size()) {
            start = objects.size();
        }

        return objects.stream().toList().subList(start, end);
    }
}
