package com.loficostudios.japaneseMinecraft;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class Debug {
    public static void log(String message) {
        Bukkit.getLogger().log(Level.INFO, "[JapaneseMinecraft] " + message);
    }
}
