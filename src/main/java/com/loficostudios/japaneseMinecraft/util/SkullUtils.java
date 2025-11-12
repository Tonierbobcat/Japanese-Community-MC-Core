package com.loficostudios.japaneseMinecraft.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class SkullUtils {
    public static ItemStack createSkull(String texture) {
        var item = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta = (SkullMeta) item.getItemMeta();

        final UUID uuid = UUID.randomUUID();
        final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
        playerProfile.setProperty(new ProfileProperty("textures", texture));

        meta.setPlayerProfile(playerProfile);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createSkull(String texture, int amount) {
        var skull = createSkull(texture);
        skull.setAmount(amount);
        return skull;
    }

    public static ItemStack getSkull(Player player) {
        var item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setPlayerProfile(player.getPlayerProfile());

        meta.displayName(Component.text("§f" + player.getName()));
        item.setItemMeta(meta);
        return item;
    }
}
