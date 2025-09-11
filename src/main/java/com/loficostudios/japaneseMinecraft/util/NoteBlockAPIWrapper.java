package com.loficostudios.japaneseMinecraft.util;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NoteBlockAPIWrapper {
    private static final Map<UUID, SongPlayer> ppp = new ConcurrentHashMap<>();
    public NoteBlockAPIWrapper(JapaneseMinecraft plugin) {
        this.plugin = plugin;
    }

    /// I am not sure if I am going to keep this
    public enum SongPlayerType {RADIO,POSITION,NOTE_BLOCK,ENTITY}

    private final JapaneseMinecraft plugin;

    private Song getSong(String key) {
        var file = new File(plugin.getDataFolder(), "songs/" + key + ".nbs");
        return NBSDecoder.parse(file);
    }

    public void playSong(String key, Player... players) {
        RadioSongPlayer rsp = new RadioSongPlayer(getSong(key));
        for (Player player : players) {
            /// If there is already a player for that player remove them
            var existing = ppp.get(player.getUniqueId());
            if (existing != null) {
                existing.removePlayer(player);
                /// check if player is empty before destroying
                if (existing.getPlayerUUIDs().isEmpty()) {
                    existing.destroy();
                }
            }
            rsp.addPlayer(player);
            ppp.put(player.getUniqueId(), rsp);
        }
        rsp.setPlaying(true);
    }
}
