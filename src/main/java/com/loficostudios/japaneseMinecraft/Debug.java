package com.loficostudios.japaneseMinecraft;

import org.bukkit.Bukkit;

import java.util.logging.Level;

/**
 * I prefer you to use this class rather than system.out.println or Bukkit.getLogger().log
 */
public class Debug {
    public static void log(String message) {
        Bukkit.getLogger().log(Level.INFO, "[JapaneseMinecraft] " + message);
    }
}
