package com.loficostudios.japaneseMinecraft;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class BountyService {
    private final File file;
    private final YamlConfiguration config;

    public BountyService(File file) {
        this.file = file;

        try {
            if (!file.exists())
                file.createNewFile();
            config = YamlConfiguration.loadConfiguration(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void post(Bounty bounty) {
        var path = "bounties." + bounty.uuid();
        config.set(path + ".message", bounty.message);
        config.set(path + ".reward", bounty.reward);
        config.set(path + ".created", bounty.created);
        try {
            config.save(file);
        } catch (IOException e) {
            Debug.logError("Could not save bounties. " + e.getMessage());
        }
    }

    public Collection<Bounty> getBounties() {
        List<Bounty> result = new LinkedList<>();
        var sect = config.getConfigurationSection("bounties");
        if (sect == null)
            return List.of();
        for (String key : sect.getKeys(false)) {
            UUID uuid = null;
            try {
                uuid = UUID.fromString(key);
            } catch (Exception e) {
                Debug.logWarning("Could not retrieve bounty. " + e.getMessage());
            }

            var message = sect.getString(key + ".message");
            var reward = sect.getDouble(key + ".reward");
            var created = sect.getLong(key + ".created");
            if (message == null) {
                Debug.logWarning("Could not retrieve bounty. " + uuid);
                continue;
            }
            result.add(new Bounty(uuid, message, reward, created));
        }

        return result;
    }

    public record Bounty(UUID uuid, String message, double reward, long created) {
        public Bounty(String message, double reward) {
            this(UUID.randomUUID(), message, reward, System.currentTimeMillis());
        }
    }
}
