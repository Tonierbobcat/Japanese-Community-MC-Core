package com.loficostudios.japaneseMinecraft.service;

import com.loficostudios.japaneseMinecraft.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class AbstractService {
    private final File file;
    private final YamlConfiguration config;

    protected AbstractService(File file) {
        this.file = file;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Debug.logError("Could not create service file: " + file.getName());
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);

        load(config);
    }

    public File getFile() {
        return file;
    }

    protected abstract void save(ConfigurationSection config);
    protected abstract void load(ConfigurationSection config);

    public void save() throws IOException {
        save(config);
        config.save(file);
    }
}
