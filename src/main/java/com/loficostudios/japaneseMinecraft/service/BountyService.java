package com.loficostudios.japaneseMinecraft.service;

import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BountyService extends AbstractService {
    private final List<Bounty> bounties = new ArrayList<>();

    public BountyService(JapaneseMinecraft plugin) {
        super(new File(plugin.getDataFolder(), "bounty.service"));
    }

    public void post(Bounty bounty) {
        bounties.add(bounty);
        try {
            save();
        } catch (IOException e) {
            Debug.logError("Could not save bounties. " + e.getMessage());
        }
    }

    public Collection<Bounty> getBounties() {
        return bounties;
    }

    @Override
    protected void save(ConfigurationSection config) {
        config.set("bounties", null);
        for (Bounty bounty : bounties) {
            var path = "bounties." + bounty.uuid();
            config.set(path + ".message", bounty.message);
            config.set(path + ".reward", bounty.reward);
            config.set(path + ".created", bounty.created);
        }
    }

    @Override
    protected void load(ConfigurationSection config) {
        var sect = config.getConfigurationSection("bounties");

        if (sect == null)
            return;
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
            bounties.add(new Bounty(uuid, message, reward, created));
        }
    }

    public record Bounty(UUID uuid, String message, double reward, long created) {
        public Bounty(String message, double reward) {
            this(UUID.randomUUID(), message, reward, System.currentTimeMillis());
        }
    }
}
