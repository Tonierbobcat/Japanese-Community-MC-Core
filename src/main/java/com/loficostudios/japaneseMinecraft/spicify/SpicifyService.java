package com.loficostudios.japaneseMinecraft.spicify;

import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.util.NoteBlockAPIWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpicifyService {
    public static final String FOLDER = "songs";
    private static final String COLOR_LEGACY = "§6";
    private static final String COLOR_MM = "<gold>";
    private static final int ITEMS_PER_PAGE = 8;
    public static final String PREFIX = Common.createMessagePrefix("Spicify", COLOR_LEGACY);

    private final JapaneseMinecraft plugin;
    
    private final NoteBlockAPIWrapper wrapper;
    
    public SpicifyService(JapaneseMinecraft plugin) {
        this.plugin = plugin;
        this.wrapper = new NoteBlockAPIWrapper(plugin);
    }

    /// We are not storing songs on the repo because of copyright infringements
    /// instead we get the songs locally by iterating through the songs folder
    public List<String> getSongKeys() {
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
    public Component getPage(List<String> songs, int page) {
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

    public void playSong(Player sender, String key) {
        wrapper.playSong(key, sender);
    }

    public boolean isListening(Player sender) {
        return NoteBlockAPIWrapper.isListening(sender);
    }

    public void stopSong(Player sender) {
        wrapper.stopSong(sender);
    }
}
