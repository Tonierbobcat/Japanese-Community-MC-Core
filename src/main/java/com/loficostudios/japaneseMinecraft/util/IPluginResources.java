package com.loficostudios.japaneseMinecraft.util;

import java.io.File;

public interface IPluginResources {
    File getJarFile();
    File getDataFolder();
    String namespace();
}
