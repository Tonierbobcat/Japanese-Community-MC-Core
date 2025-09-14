package com.loficostudios.japaneseMinecraft;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Messages {

    private static final String WELCOME_MESSAGE_EN = """
             Welcome, {player}, to the JP-ENG community server!
             This server is a work in progress. Features may be added or changed over time.
             If you have any suggestions, please use /jpmc suggest <suggestion>
             Enjoy your time here!
             """;
    private static final String WELCOME_MESSAGE_JP = """
            ようこそ、{player}さん、JP-ENGコミュニティサーバーへ！
            このサーバーは進行中のプロジェクトです。機能は時間とともに追加または変更される場合があります。
            ご提案がございましたら、/jpmc suggest <提案> をご利用ください。
            ここでの時間をお楽しみください！
            """;

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

    /// Shiritori game messages
    private static final String CANNOT_USE_WORD_ENDING_IN_N_EN = "You cannot use a word that ends with 'ん'. You lost {points} point(s)!";
    private static final String CANNOT_USE_WORD_ENDING_IN_N_JP = "「ん」で終わる言葉は使えません。{points}ポイント失います！";

    private static final String CANNOT_USE_WORD_STARTING_IN_WRONG_KANA_EN = "Your word must start with '{kana}' (romaji: {romaji}). You lost {points} point(s)!";
    private static final String CANNOT_USE_WORD_STARTING_IN_WRONG_KANA_JP = "あなたの言葉は'{kana}'（ローマ字: {romaji}）で始まらなければなりません。{points}ポイント失います！";

    private static final String ALREADY_USED_WORD_EN = "This word has already been used. Please try another word. You lost {points} point(s)!";
    private static final String ALREADY_USED_WORD_JP = "この言葉はすでに使われています。別の言葉を試してください。{points}ポイント失います！";

    private static final String WORD_CORRECT_EN = "You get {points} point(s)!";
    private static final String WORD_CORRECT_JP = "{points}ポイント獲得！";

    /// Monster Pal System
    private static final String CANNOT_CATCH_OTHERS_CREATURES_EN = "You cannot catch other people's Creatures";
    //TODO JAPANESE

    private static final String RETRIEVED_CREATURE_EN = "You retrieved your level {level} {name}";
    //TODO JAPANESE

    private static final String CREATURE_CAUGHT_EN = "You caught a level {level} {name}";
    //TODO JAPANESE

    private static final String FAILED_CATCH_EN = "You failed to catch this MON. {probability}%";
    //TODO JAPANESE

    private static final String UNCATCHABLE_CREATURE_EN = "Cannot catch this MON";
    //TODO JAPANESE

    private static final String ITEM_LEVEL_CANDY_NOT_OWNED_EN = "You do not own this Creature!";
    //TODO JAPANESE

    private static final String ITEM_LEVEL_CANDY_MAX_LEVEL_EN = "This Creature is already at max level!";
    //TODO JAPANESE

    private static final String ITEM_LEVEL_CANDY_LEVELUP_EN = "Leveled up this Creature!";
    //TODO JAPANESE

    /// Spicify
    private static final String STOPPED_LISTENING_EN = "Stopped listening...";
    //TODO JAPANESE

    private static final String NOT_LISTENING_TO_ANYTHING_EN = "You were not listening to anything";
    //TODO JAPANESE

    /// Ideally I do not want color codes in messages but this looks really nice
    private static final String NOW_PLAYING_EN = "Now Playing §6{song}§r...";
    //TODO JAPANESE

    private static final String MUST_ENTER_VALID_SONG_ID_EN = "You must enter a valid song. '{key}' If you think this a mistake please contact admins";
    //TODO JAPANESE


    public static String getMessage(Player player, String key) {
        boolean isJapanese = JapaneseMinecraft.isPlayerLanguageJapanese(player);
        key = key + (isJapanese ? "_jp" : "_en");
        var message = messages.get(key);
        return message != null && !message.isEmpty() ? message : (isJapanese ? ERROR_MESSAGE_JP : ERROR_MESSAGE)
                .replace("<key>", key);
    }

    static {
        messages = new HashMap<>();

        messages.put("stopped_listening_en", STOPPED_LISTENING_EN);
        //TODO JAPANESE

        messages.put("not_listening_to_anything_en", NOT_LISTENING_TO_ANYTHING_EN);
        //TODO JAPANESE

        messages.put("now_playing_en", NOW_PLAYING_EN);
        //TODO JAPANESE

        messages.put("must_enter_valid_song_id_en", MUST_ENTER_VALID_SONG_ID_EN);
        //TODO JAPANESE


        messages.put("item_level_candy_not_owned_en", ITEM_LEVEL_CANDY_NOT_OWNED_EN);
        //TODO JAPANESE

        messages.put("item_level_candy_max_level_en", ITEM_LEVEL_CANDY_MAX_LEVEL_EN);
        //TODO JAPANESE

        messages.put("item_level_candy_levelup_en", ITEM_LEVEL_CANDY_LEVELUP_EN);
        //TODO JAPANESE

        messages.put("uncatchable_creature_en", UNCATCHABLE_CREATURE_EN);
        //TODO JAPANESE

        messages.put("failed_catch_en", FAILED_CATCH_EN);
        //TODO JAPANESE

        messages.put("creature_caught_en", CREATURE_CAUGHT_EN);
        //TODO JAPANESE

        messages.put("retrieved_creature_en", RETRIEVED_CREATURE_EN);
        //TODO JAPANESE

        messages.put("cannot_catch_others_creatures_en", CANNOT_CATCH_OTHERS_CREATURES_EN);
        //TODO JAPANESE

        messages.put("welcome_message_en", WELCOME_MESSAGE_EN);
        messages.put("welcome_message_jp", WELCOME_MESSAGE_JP);

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


        messages.put("word_ends_with_n_en", CANNOT_USE_WORD_ENDING_IN_N_EN);
        messages.put("word_ends_with_n_jp", CANNOT_USE_WORD_ENDING_IN_N_JP);

        messages.put("word_already_used_en", ALREADY_USED_WORD_EN);
        messages.put("word_already_used_jp", ALREADY_USED_WORD_JP);

        messages.put("wrong_kana_en", CANNOT_USE_WORD_STARTING_IN_WRONG_KANA_EN);
        messages.put("wrong_kana_jp", CANNOT_USE_WORD_STARTING_IN_WRONG_KANA_JP);

        messages.put("word_correct_en", WORD_CORRECT_EN);
        messages.put("word_correct_jp", WORD_CORRECT_JP);

    }
}
