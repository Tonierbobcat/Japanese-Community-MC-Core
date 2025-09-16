package com.loficostudios.japaneseMinecraft;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Messages {

    private static final Map<String, String> messages = new HashMap<>();

    private static final String ERROR_MESSAGE = "!!!ERROR!!! Please report this to a server admin: missing message title <title>";
    private static final String ERROR_MESSAGE_JP = "サーバー管理者にこれを報告してください: メッセージキーが見つかりません <title>";

    public static void initialize(JapaneseMinecraft plugin) throws IOException {
        plugin.saveResource("en_us.yml", true);
        plugin.saveResource("ja_jp.yml", true);

        var english = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "en_us.yml"));
        var japanese = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "ja_jp.yml"));

        for (Key key : Key.values()) {
            var enText = english.getString(key.name().toLowerCase());
            var jpText = japanese.getString(key.name().toLowerCase());

            if (enText != null)
                messages.put(key.name().toLowerCase() + "_en", enText);

            if (jpText != null)
                messages.put(key.name().toLowerCase() + "_jp", jpText);
        }
    }

    public static String getMessage(Player player, Key key) {
        boolean isJapanese = JapaneseMinecraft.isPlayerLanguageJapanese(player);
        var stringKey = key.name().toLowerCase() + (isJapanese ? "_jp" : "_en");
        var message = messages.get(stringKey);
        return message != null && !message.isEmpty() ? message : (isJapanese ? ERROR_MESSAGE_JP : ERROR_MESSAGE)
                .replace("<title>", stringKey);
    }

    @Deprecated
    public static String getMessage(Player player, String key) {
        boolean isJapanese = JapaneseMinecraft.isPlayerLanguageJapanese(player);
        key = key + (isJapanese ? "_jp" : "_en");
        var message = messages.get(key);
        return message != null && !message.isEmpty() ? message : (isJapanese ? ERROR_MESSAGE_JP : ERROR_MESSAGE)
                .replace("<title>", key);
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
