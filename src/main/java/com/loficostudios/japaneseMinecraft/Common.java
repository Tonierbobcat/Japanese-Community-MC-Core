package com.loficostudios.japaneseMinecraft;

import com.loficostudios.japaneseMinecraft.util.JishoResponse;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.permissions.ServerOperator;

public class Common {
    public static void notifyPlayers(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    /// This method is used to notify all server admins of an error.
    /// These is usually errors that are reported during runtime of the server and not during initialization.
    public static void notifyAdmins(String message) {
        Bukkit.getOnlinePlayers().stream()
                .filter(ServerOperator::isOp)
                .forEach(p -> p.sendMessage(message));
    }

    /**
     *
     * @throws IllegalArgumentException if name is null
     */
    public static String formatEnumName(String name) {
        Validate.isTrue(name != null);
        var builder = new StringBuilder();
        var strings = name.toLowerCase().split("_");
        for (String string : strings) {
            var chars = string.toCharArray();
            if (chars.length == 0)
                continue;
            chars[0] = Character.toUpperCase(chars[0]);
            builder.append(chars).append(" ");
        }
        return builder.toString().trim();
    }

    public static <T extends Enum<T>> String formatEnumName(Enum<T> tEnum) {
        return formatEnumName(tEnum.name());
    }

    public static String getDictionaryMessageFromResult(String[][] result) { // need to pass in JishoResponse instead and have a parameter for pagination
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

    /// Placeholder for future pagination support
    public static String getDictionaryMessageFromResult(JishoResponse response, int page) {
        return "";
    }

    public static String createMessagePrefix(String title, String color) {
        return "§8[{color}{title}§8]§r "
                .replace("{color}", color)
                .replace("{title}", title);
    }
}
