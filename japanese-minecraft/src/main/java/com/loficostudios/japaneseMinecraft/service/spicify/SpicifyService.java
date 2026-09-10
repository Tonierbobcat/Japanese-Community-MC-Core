package com.loficostudios.japaneseMinecraft.service.spicify;

import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.service.AbstractService;
import com.loficostudios.japaneseMinecraft.util.NoteBlockAPIWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SpicifyService extends AbstractService {
    public static final String FILE = "spicify.service";
    public static final String FOLDER = "songs";
    private static final String COLOR_LEGACY = "§6";
    private static final String COLOR_MM = "<gold>";
    private static final int ITEMS_PER_PAGE = 8;
    public static final String PREFIX = Common.createMessagePrefix("Spicify", COLOR_LEGACY);

    private final NoteBlockAPIWrapper wrapper;

    private final Map<Integer, SpicifySong> songs;

    private final Map<SpicifySong, Set<UUID>> likes = new HashMap<>();

    public SpicifyService(JapaneseMinecraft plugin) {
        super(new File(plugin.getDataFolder(), FILE));
        this.wrapper = new NoteBlockAPIWrapper();

        songs = NoteBlockAPIWrapper.initialize(this, new File(plugin.getDataFolder(), FOLDER));
    }

    public List<Integer> getSongIds() {
        return new ArrayList<>(songs.keySet());
    }

    public List<SpicifySong> getAll() {
        return new ArrayList<>(songs.values());
    }

    public List<SpicifySong> search(String query) {
        if (!query.isEmpty())
            query = query.toLowerCase().trim();
        List<SpicifySong> results = new ArrayList<>();
        for (Map.Entry<Integer, SpicifySong> entry : songs.entrySet()) {
            var song = entry.getValue();
            if (!query.isEmpty() && !song.title().toLowerCase().contains(query))
                continue;

            results.add(song);
        }
        return results;
    }

    /// page starts at 0
    public Component getPage(List<SpicifySong> songs, int page, String command) {
        var header = "-- {prefix} {current-page}/{max-page} --";

        var prevPage = page > 0
                ? "<click:run_command:'/"  + command.replace("{page}", "" + (page - 1)) + "'><yellow>« Prev</yellow></click>"
                : "<gray>« Prev</gray>";

        var nextPage = page < getMaxPage(songs)
                ? "<click:run_command:'/" + command.replace("{page}", "" + (page + 1)) + "'><yellow>Next »</yellow></click>"
                : "<gray>Next »</gray>";

        var footer = prevPage + " <gray>|</gray> " + nextPage;

        List<String> format = new ArrayList<>();

        List<SpicifySong> paginated = new ArrayList<>(paginate(songs, (page - 1), ITEMS_PER_PAGE));

        /// Trim the prefix and replace legacy code with mm format
        format.add(header
                .replace("{prefix}", PREFIX.trim().replace(COLOR_LEGACY, COLOR_MM).replace("§8", "<gray>").replace("§r", "<reset>"))
                .replace("{current-page}", "" + page)
                .replace("{max-page}", "" + getMaxPage(songs)));

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            var line = "  - {song} [{play-icon}<reset>][<green>+<reset>][<red>-<reset>][{like-icon}<reset>]";

            if (i > paginated.size() - 1)
                continue;

            var song = paginated.get(i);
            var id = song.id();

            var playIcon = "<click:run_command:/spicify play {song-id}>{spicify-color}▶</click>"
                    .replace("{song-id}", "" + id);

//            var likeIcon = "<click:run_command:/spicify like {song-id}><red>❤<reset> <bold>{likes}</click>"
//                    .replace("{likes}", "" + song.likes());

            format.add(line
                    .replace("{like-icon}", "<red>❤")
                    .replace("{play-icon}", playIcon.replace("{spicify-color}", COLOR_MM))
                    .replace("{song}", song.title()));
        }

        format.add(footer);

        return MiniMessage.miniMessage().deserialize(String.join("\n", format));
    }

    public int getLikes(SpicifySong song) {
        return likes.getOrDefault(song, Collections.emptySet()).size();
    }

    /**
     *
     * @return {@code false} if the player already liked the song
     */
    public boolean likeSong(Player player, SpicifySong song) {
        Set<UUID> songLikes = likes.computeIfAbsent(song, s -> new HashSet<>());

        if (!songLikes.add(player.getUniqueId())) {
            /// already liked by the player
            return false;
        }
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean unlikeSong(Player player, SpicifySong song) {
        Set<UUID> songLikes = likes.get(song);
        if (songLikes != null) {
            var removed = songLikes.remove(player.getUniqueId());

            /// remove song from likes map
            if (songLikes.isEmpty())
                likes.remove(song);
            return removed;
        }
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private <T> int getMaxPage(List<T> objects) {
        if (objects == null || objects.isEmpty()) {
            return 1; // at least 1 page, even if no songs
        }
        return (objects.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
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

    public boolean playSong(Player sender, SpicifySong song) {
        return wrapper.playSong(NoteBlockAPIWrapper.InstanceType.SPICIFY, song.id(), sender);
    }

    @Deprecated
    public void playSong(Player sender, int id) {
        wrapper.playSong(NoteBlockAPIWrapper.InstanceType.SPICIFY, id, sender);
    }

    public boolean isListening(Player sender) {
        return NoteBlockAPIWrapper.isListening(NoteBlockAPIWrapper.InstanceType.SPICIFY, sender);
    }

    public void stopSong(Player sender) {
        wrapper.stopSong(NoteBlockAPIWrapper.InstanceType.SPICIFY, sender);
    }

    public @Nullable SpicifySong getCurrentSong(Player sender) {
        var id = wrapper.getCurrentSongId(NoteBlockAPIWrapper.InstanceType.SPICIFY, sender);
        if (id == -1)
            return null;
        return songs.get(id);
    }

    public @Nullable SpicifySong getSong(int id) {
        return songs.get(id);
    }

    @Override
    protected void save(ConfigurationSection config) {
    }

    @Override
    protected void load(ConfigurationSection config) {
    }
}
