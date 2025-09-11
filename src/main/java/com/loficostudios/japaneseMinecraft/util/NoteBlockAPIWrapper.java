package com.loficostudios.japaneseMinecraft.util;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import org.bukkit.entity.Player;

import java.io.File;

public class NoteBlockAPIWrapper {
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
            rsp.addPlayer(player);
        }
        rsp.setPlaying(true);
    }
}
