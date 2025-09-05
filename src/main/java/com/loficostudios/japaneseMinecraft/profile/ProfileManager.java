package com.loficostudios.japaneseMinecraft.profile;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileManager implements Listener {
    private final Map<UUID, PlayerProfile> profiles = new HashMap<>();
    private final JapaneseMinecraft plugin;

    public ProfileManager(JapaneseMinecraft plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        if (profiles.containsKey(player.getUniqueId())) {
            return;
        }
        profiles.put(player.getUniqueId(), new PlayerProfile(plugin, player));
    }

    public PlayerProfile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    public PlayerProfile getProfile(UUID uuid) {
        return profiles.get(uuid);
    }
}
