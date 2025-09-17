package com.loficostudios.japaneseMinecraft;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Messages {

    private static final Map<String, String> messages = new HashMap<>();

    private static final String ERROR_MESSAGE = "!!!ERROR!!! Please report this to a server admin: missing message key <key>";
    private static final String ERROR_MESSAGE_JP = "サーバー管理者にこれを報告してください: メッセージキーが見つかりません <key>";

    public static void initialize(JapaneseMinecraft plugin) throws IOException {
        plugin.saveResource("en_us.yml", true);
        plugin.saveResource("ja_jp.yml", true);

        var english = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "en_us.yml"));
        var japanese = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "ja_jp.yml"));

        /// load native messages first
        for (Key key : Key.values()) {
            handleEnglish(english, key.name().toLowerCase());
            handleJapanese(japanese, key.name().toLowerCase());
        }

        /// this lets the plugin load messages that are not in the keys enum
        for (String key : english.getKeys(false)) {
            if (messages.containsKey(key + "_en"))
                continue;
            handleEnglish(english, key);
        }

        for (String key : japanese.getKeys(false)) {
            if (messages.containsKey(key + "_jp"))
                continue;
            handleJapanese(japanese, key);
        }
    }

    private static void handleJapanese(ConfigurationSection japanese, String key) {
        var jpText = japanese.getString(key);
        if (jpText != null)
            messages.put(key + "_jp", jpText);
    }

    private static void handleEnglish(ConfigurationSection english, String key) {
        var enText = english.getString(key);
        if (enText != null)
            messages.put(key + "_en", enText);
    }

    public static String getMessage(Player player, Key key) {
        return getMessage(player, key.name().toLowerCase());
    }

    @Deprecated
    public static String getMessage(Player player, String key) {
        boolean isJapanese = JapaneseMinecraft.isPlayerLanguageJapanese(player);
        key = key.toLowerCase() + (isJapanese ? "_jp" : "_en");
        var message = messages.get(key);
        return message != null && !message.isEmpty() ? message : (isJapanese ? ERROR_MESSAGE_JP : ERROR_MESSAGE)
                .replace("<key>", key);
    }

    public enum Key {
        WELCOME_MESSAGE,
        GLOBAL_PLAYER_REVIVED,
        PLAYER_REVIVED,
        YOU_LOST_EXPERIENCE,
        SUGGESTION_HINT,
        CANNOT_SUBMIT_EMPTY_SUGGESTION,
        FAILED_TO_SUBMIT_SUGGESTION,
        SUCCESSFULLY_SUBMITTED_SUGGESTION,
        CANNOT_PVP,
        WORD_ENDS_WITH_N,
        WRONG_KANA,
        ALREADY_USED_WORD,
        WORD_CORRECT,
        CANNOT_CATCH_OTHERS_CREATURES,
        RETRIEVED_CREATURE,
        CREATURE_CAUGHT,
        FAILED_CATCH,
        UNCATCHABLE_CREATURE,
        ITEM_LEVEL_CANDY_NOT_OWNED,
        ITEM_LEVEL_CANDY_MAX_LEVEL,
        ITEM_LEVEL_CANDY_LEVELUP,
        STOPPED_LISTENING,
        NOT_LISTENING_TO_ANYTHING,
        NOW_PLAYING,
        MUST_ENTER_VALID_SONG_ID
    }
}
