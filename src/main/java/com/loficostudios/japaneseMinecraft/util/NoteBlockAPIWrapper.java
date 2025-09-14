package com.loficostudios.japaneseMinecraft.util;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.spicify.SpicifyService;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/// I don't want noteblock api class to be integrated in the repo.
/// instead we are going to use this wrapper class
public class NoteBlockAPIWrapper {
    private static final Map<UUID, SongPlayer> ppp = new ConcurrentHashMap<>();
    public NoteBlockAPIWrapper(JapaneseMinecraft plugin) {
        this.plugin = plugin;
    }

    public static boolean isListening(Player sender) {
        return ppp.containsKey(sender.getUniqueId());
    }

    public String getCurrentSong(Player sender) {
        var player = ppp.get(sender.getUniqueId());
        if (player == null)
            return null;
        var title = player.getSong().getTitle();
        var key = player.getSong().getPath();
        return key.getName();
    }

    /// I am not sure if I am going to keep this
    public enum SongPlayerType {RADIO,POSITION,NOTE_BLOCK,ENTITY}

    private final JapaneseMinecraft plugin;

    private Song getSong(String key) {
        var file = new File(plugin.getDataFolder(), SpicifyService.FOLDER + "/" + key + ".nbs");
        return NBSDecoder.parse(file);
    }

    public void stopSong(Player... players) {
        for (Player player : players) {
            a(player);
        }
    }

    /* IDK what do call this */
    /// If there is already a player for that player remove them
    private void a(Player player) {
        var existing = ppp.remove(player.getUniqueId());
        if (existing != null) {
            existing.removePlayer(player);
            /// check if player is empty before destroying
            if (existing.getPlayerUUIDs().isEmpty()) {
                existing.destroy();
            }
        }
    }

    public void playSong(String key, Player... players) {
        RadioSongPlayer rsp = new RadioSongPlayer(getSong(key));
        for (Player player : players) {
            a(player);

            rsp.addPlayer(player);
            ppp.put(player.getUniqueId(), rsp);
        }
        rsp.setPlaying(true);
    }
}
