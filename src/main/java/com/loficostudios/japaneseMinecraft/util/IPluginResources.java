package com.loficostudios.japaneseMinecraft.util;

import org.bukkit.Server;

import java.io.File;

public interface IPluginResources {
    File getJarFile();
    File getDataFolder();
    String namespace();
    Server getServer();
}
