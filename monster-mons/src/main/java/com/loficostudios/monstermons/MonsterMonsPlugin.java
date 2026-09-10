package com.loficostudios.monstermons;

import com.loficostudios.forgified.paper.IPluginResources;
import com.loficostudios.forgified.paper.items.JItem;
import com.loficostudios.forgified.registry.Registries;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MonsterMonsPlugin extends JavaPlugin implements IPluginResources {

    @Override
    public void onLoad() {
        saveDefaultConfig();
        var items = Registries.create(JItem.class, this);
        var itemSection = getConfig().getConfigurationSection("items");
        for (String key : itemSection.getKeys(false)) {
            var path = key + ".";
            var type = itemSection.getString(path + "type");
            if (type.equals("generic")) {
                var material = Material.matchMaterial(itemSection.getString(path + "material").toUpperCase());
                items.register(key, () -> new JItem(material, JItem.Properties.empty()));
            } else if (type.equals("ball")) {
                var capturePower = itemSection.getDouble(path + "capturePower");
                items.register(key, () -> new MonsterBall(capturePower, JItem.Properties.empty()));
            }
        }
        items.register(this);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new MonsterBallListener(this), this);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public File getJarFile() {
        return getFile();
    }
}
