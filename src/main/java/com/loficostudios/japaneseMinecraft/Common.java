package com.loficostudios.japaneseMinecraft;

import org.bukkit.Bukkit;
import org.bukkit.permissions.ServerOperator;

public class Common {
    public static void notifyPlayers(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    public static void notifyAdmins(String message) {
        Bukkit.getOnlinePlayers().stream()
                .filter(ServerOperator::isOp)
                .forEach(p -> p.sendMessage(message));
    }

    public static String getDictionaryMessageFromResult(String[][] result) {
        final String template = """
            §a§l=== Dictionary Result ===
            §eWord: §f{word} ({reading})
            §eDefinitions: §f{definition}
            §a§l======================""";

        var readings = result[1].length > 0 ? String.join(", ", result[1]): null;
        var word = result[0] != null ? String.join(", ", result[0]) : readings;
        var definitions = result[2] != null ? result[2] : new String[0];

        return template
                .replace("{word}", word != null ? word : "<null>")
                .replace("{reading}", readings != null ? readings : "<null>")
                .replace("{definition}", definitions.length > 0 ? "\n    " + String.join("\n    ", definitions) : "<null>");
    }
}
