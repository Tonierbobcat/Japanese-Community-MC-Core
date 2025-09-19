package com.loficostudios.japaneseMinecraft.util;

import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.spicify.SpicifyService;
import com.loficostudios.japaneseMinecraft.spicify.SpicifySong;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.NoteBlockSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import org.apache.commons.lang3.Validate;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/// I don't want noteblock api class to be integrated in the repo.
/// instead we are going to use this wrapper class
public class NoteBlockAPIWrapper {
    private static final Map<InstanceType, Map<UUID, SongPlayer>> ppp = new ConcurrentHashMap<>();
    public static final Map<Integer, File> indexed = new HashMap<>();
    public static final Map<Integer, Song> cached = new HashMap<>();

    private static boolean initialized;

    /// we use this function because we shouldnt be calling from ppp directly
    private static @NotNull Map<UUID, SongPlayer> getSongPlayers(InstanceType instance) {
        return ppp.computeIfAbsent(instance, (inst) -> new HashMap<>());
    }

    public static boolean isListening(InstanceType instance, Player sender) {
        return getSongPlayers(instance).containsKey(sender.getUniqueId());
    }

    public int getCurrentSongId(InstanceType instance, Player sender) {
        SongPlayer player = getSongPlayers(instance).get(sender.getUniqueId());
        if (player == null)
            return -1;
        var song = player.getSong();

        for (Map.Entry<Integer, Song> entry : cached.entrySet()) {
            if (entry.getValue().equals(song))
                return entry.getKey();
        }
        return -1;
    }

    /// only time spicify songs are referenced in this class
    public static Map<Integer, SpicifySong> initialize(SpicifyService service, File songFolder) {
        Validate.isTrue(!initialized);
        initialized = true;

        Map<Integer, SpicifySong> result = new HashMap<>();
        var files = songFolder.listFiles();
        if (files == null)
            return Map.of();
        for (File file : files) {
            var title = file.getName().replace(".nbs", "");
            var id = result.size();

            result.put(id, new SpicifySong(service, id, title));
            indexed.put(id, file);
        }

        Debug.log("Loaded " + result.size() + " songs");

        return result;
    }

    /// I am not sure if I am going to keep this
    @Deprecated
    public enum SongPlayerType {RADIO,POSITION,NOTE_BLOCK,ENTITY}

    public enum InstanceType {SPICIFY,GLOBAL}

    public void stopSong(InstanceType instance, Player... players) {
        for (Player player : players) {
            a(instance, player);
        }
    }

    /* IDK what do call this */
    /// If there is already a player for that player remove them
    private void a(InstanceType instance, Player player) {
        var existing = getSongPlayers(instance).remove(player.getUniqueId());
        if (existing != null) {
            existing.removePlayer(player);
            /// check if player is empty before destroying
            if (existing.getPlayerUUIDs().isEmpty()) {
                existing.destroy();
            }
        }
    }

    /// it would be smart to add logging here for admins to debug
    private Song getSongFromId(int songId) {
        /// if cached exists return
        var existing = cached.get(songId);
        if (existing != null)
            return existing;

        var file = indexed.get(songId);
        if (file == null)
            return null;

        var song = NBSDecoder.parse(file);
        if (song == null)
            return null;

        /// cache song
        cached.put(songId, song);
        return song;
    }

    public boolean playSong(InstanceType instance, int songId, Player... players) {
        var song = getSongFromId(songId);
        if (song == null)
            return false;
        RadioSongPlayer rsp = new RadioSongPlayer(song);
        for (Player player : players) {
            a(instance, player);

            rsp.addPlayer(player);

            getSongPlayers(instance).put(player.getUniqueId(), rsp);
        }
        rsp.setPlaying(true);
        return true;
    }
}
