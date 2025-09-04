package com.loficostudios.japaneseMinecraft;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Messages {


    private static final String WORD_USED_EN = "This word has already been used. Please try another word.";
    private static final String WORD_USED_JP = "この言葉はもう使われました。別の言葉を試してください。";

    private static final String WORD_INVALID_EN = "Invalid word. Please try again.";
    private static final String WORD_INVALID_JP = "無効な言葉です。もう一度試してください。";

    private static final String GLOBAL_PLAYER_REVIVED_EN = "%s has been revived by a mysterious garden spirit!";
    private static final String GLOBAL_PLAYER_REVIVED_JP = "%sは神秘的な庭の精霊によって復活しました！";

    private static final String PLAYER_REVIVED_EN = "You have been revived.";
    private static final String PLAYER_REVIVED_JP = "あなたは復活しました。";

    private static final String YOU_LOST_EXPERIENCE_EN = "You have lost all of your experience.";
    private static final String YOU_LOST_EXPERIENCE_JP = "あなたはすべての経験を失いました";

    private static final String SUGGESTION_HINT_EN = "Want something to be added to the server? Use /jpmc suggest <your suggestion>";
    private static final String SUGGESTION_HINT_JP = "サーバーに追加したいものがありますか？ /jpmc suggest <あなたの提案> を使用してください";

    private static final String CANNOT_SUBMIT_EMPTY_SUGGESTION_EN = "Could not submit. Your suggestion is empty.";
    private static final String CANNOT_SUBMIT_EMPTY_SUGGESTION_JP = "送信できません。あなたの提案は空です。";

    private static final String FAILED_TO_SUBMIT_SUGGESTION_EN = "Failed to save your suggestion. Please try again later.";
    private static final String FAILED_TO_SUBMIT_SUGGESTION_JP = "提案の保存に失敗しました。後でもう一度お試しください。";

    private static final String SUCCESSFULLY_SUBMITTED_SUGGESTION_EN = "Thank you for your suggestion!";
    private static final String SUCCESSFULLY_SUBMITTED_SUGGESTION_JP = "ご提案ありがとうございます！";

    private static final Map<String, String> messages;

    private static final String ERROR_MESSAGE = "!!!ERROR!!! Please report this to a server admin: missing message key <key>";
    private static final String ERROR_MESSAGE_JP = "サーバー管理者にこれを報告してください: メッセージキーが見つかりません <key>";

    private static final String CANNOT_PVP_EN = "You cannot PvP on this server.";
    private static final String CANNOT_PVP_JP = "このサーバーではPvPできません。";

    public static String getMessage(Player player, String key) {
        boolean isJapanese = JapaneseMinecraft.isPlayerLanguageJapanese(player);
        key = key + (isJapanese ? "_jp" : "_en");
        return messages.getOrDefault(key, (isJapanese ? ERROR_MESSAGE_JP : ERROR_MESSAGE)
                .replace("<key>", key));
    }

    static {
        messages = new HashMap<>();
        messages.put("word_used_en", WORD_USED_EN);
        messages.put("word_used_jp", WORD_USED_JP);

        messages.put("word_invalid_en", WORD_INVALID_EN);
        messages.put("word_invalid_jp", WORD_INVALID_JP);

        messages.put("global_player_revived_en", GLOBAL_PLAYER_REVIVED_EN);
        messages.put("global_player_revived_jp", GLOBAL_PLAYER_REVIVED_JP);

        messages.put("you_lost_experience_en", YOU_LOST_EXPERIENCE_EN );
        messages.put("you_lost_experience_jp", YOU_LOST_EXPERIENCE_JP);

        messages.put("suggestion_hint_en", SUGGESTION_HINT_EN);
        messages.put("suggestion_hint_jp", SUGGESTION_HINT_JP);

        messages.put("player_revived_en", PLAYER_REVIVED_EN);
        messages.put("player_revived_jp", PLAYER_REVIVED_JP);

        messages.put("cannot_submit_empty_suggestion_en", CANNOT_SUBMIT_EMPTY_SUGGESTION_EN);
        messages.put("cannot_submit_empty_suggestion_jp", CANNOT_SUBMIT_EMPTY_SUGGESTION_JP);

        messages.put("failed_to_submit_suggestion_en", FAILED_TO_SUBMIT_SUGGESTION_EN);
        messages.put("failed_to_submit_suggestion_jp", FAILED_TO_SUBMIT_SUGGESTION_JP);

        messages.put("successfully_submitted_suggestion_en", SUCCESSFULLY_SUBMITTED_SUGGESTION_EN);
        messages.put("successfully_submitted_suggestion_jp", SUCCESSFULLY_SUBMITTED_SUGGESTION_JP);

        messages.put("cannot_pvp_en", CANNOT_PVP_EN);
        messages.put("cannot_pvp_jp", CANNOT_PVP_JP);

    }
}
