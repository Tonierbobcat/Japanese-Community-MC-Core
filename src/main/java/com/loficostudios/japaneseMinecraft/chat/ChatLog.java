package com.loficostudios.japaneseMinecraft.chat;

import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ChatLog {
//    private final String DM_LOGS_FOLDER = "logs/chat/dms";
    private final String GLOBAL_LOGS_FOLDER = "logs/chat/global";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final JapaneseMinecraft plugin;

//    private final File dmLogFile;
//    private final ConfigurationSection dmLogConfig;

    private File globalLogFile;
    private YamlConfiguration globalLogConfig;

    private boolean silent;

    private long lastGlobalLog;

    public ChatLog(JapaneseMinecraft plugin) {
        this.plugin = plugin;
        createNewGlobalLogFile();
    }

    private void createNewGlobalLogFile() {
        var timestamp = System.currentTimeMillis();

        String date = dateFormatter.format(Instant.ofEpochMilli(timestamp));
        globalLogFile = new File(plugin.getDataFolder(), GLOBAL_LOGS_FOLDER + "/"+ date + ".log");
        globalLogFile.getParentFile().mkdirs();
        if (!globalLogFile.exists())  {
            try {
                globalLogFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        globalLogConfig = YamlConfiguration.loadConfiguration(globalLogFile);
    }

    private void logGlobal(long currentTimeMillis, String time, String name, String message) {
        lastGlobalLog = currentTimeMillis;
        String key = time + "_" + System.nanoTime() + "." + name + ".message";
        globalLogConfig.set(key, message);
        try {
            globalLogConfig.save(globalLogFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isNewDay(long currentTimeMillis) {
        String currentDate = dateFormatter.format(Instant.ofEpochMilli(currentTimeMillis));
        String lastDate = dateFormatter.format(Instant.ofEpochMilli(lastGlobalLog));

        return !currentDate.equals(lastDate);
    }

    public synchronized void log(Player sender, String message) {
        long currentTimeMillis = System.currentTimeMillis();

        String time = timeFormatter.format(Instant.ofEpochMilli(currentTimeMillis));

        if (lastGlobalLog > 0 && isNewDay(currentTimeMillis)) {
            createNewGlobalLogFile();
        }
        logGlobal(currentTimeMillis, time, sender.getName(), message);

        if (!silent) {
            Debug.log("[{time}] {player} -> {message}"
                    .replace("{time}", time)
                    .replace("{player}", sender.getName())
                    .replace("{message}", message));
        }
    }

    public ChatLog setSilent(boolean silent) {
        this.silent = silent;
        return this;
    }
}
