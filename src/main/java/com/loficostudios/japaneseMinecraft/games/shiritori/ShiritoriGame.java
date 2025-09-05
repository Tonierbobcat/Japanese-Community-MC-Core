package com.loficostudios.japaneseMinecraft.games.shiritori;

import com.github.jikyo.romaji.Transliterator;
import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.Messages;
import com.loficostudios.japaneseMinecraft.util.JishoAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class ShiritoriGame { // PLACEHOLDER ADD LATER

    /// These words must be written in kana (hiragana or katakana)
    private final static String[] DEFAULT_WORDS = {
            "ねこ",      // 猫
            "いぬ",      // 犬
            "さくら",    // 桜
            "くも",      // 雲
            "とり",      // 鳥
            "はな",      // 花
            "みず",      // 水
            "やま",      // 山
            "うみ",      // 海
            "き",        // 木
            "ほし",      // 星
            "かさ",      // 傘
            "くるま",    // 車
            "ともだち",  // 友達
            "うさぎ",    // 兎
            "こいぬ",    // 子犬
            "りょこう",  // 旅行
            "こい",      // 鯉
            "ふね",      // 船
            "とけい",    // 時計
            "かわいい",  // かわいい
            "ひかり",    // 光
            "あめ",      // 雨
            "でんわ",    // 電話
            "ゆき"       // 雪
    };

    private static final String PREFIX = "§8[§dShiritori§8] §r";

    private static final String STARTING_MESSAGE = """
        §a§l=== Shiritori Starting ===
        §eThe game will last for §f{minutes} §eminute(s).
        §eThe player with the most points at the end wins!
        
        §eThe first word is: §f'{word}'
        §a§l=========================
        """;

    private static final String PLAYER_GUESSED_MESSAGE = "{player} gets a point!";

    private static final int CORRECT_POINTS = 2;

    private static final int INCORRECT_POINTS = 1;

    /// Stores the japanese reading of the used words
    private final Set<String> usedWords = new LinkedHashSet<>();

    /// Conccurency is needed because multiple players can submit words at the same time
    private final Map<UUID, Integer> scores = new ConcurrentHashMap<>();

    private final int gameLengthMinutes;

    public ShiritoriGame(int gameLengthMinutes) {
        this.gameLengthMinutes = gameLengthMinutes;
    }

    public void start() {
        var word = DEFAULT_WORDS[ThreadLocalRandom.current().nextInt(DEFAULT_WORDS.length)];
        usedWords.add(word);

        Common.notifyPlayers(STARTING_MESSAGE.replace("{word}", word).replace("{minutes}", "" + gameLengthMinutes));
    }

    /// Get the last kana of the last used word
    private String getLastKana() {
        String word = usedWords.stream().reduce((first, second) -> second).orElse("");
        if (word.isEmpty()) return "";

        int lastIndex = word.length() - 1;
        char lastChar = word.charAt(lastIndex);

        String modifiers = "ゃゅょァィゥェォャュョッ゛゜";

        if (modifiers.indexOf(lastChar) != -1 && lastIndex > 0) {
            return word.substring(lastIndex - 1, lastIndex + 1);
        }

        return String.valueOf(lastChar);
    }

    private void subtractPoints(Player player, int points) {
        var currentScore = scores.getOrDefault(player.getUniqueId(), 0);
        scores.put(player.getUniqueId(), Math.max(0, currentScore - points));
    }

    private void addPoints(Player player, int points) {
        var currentScore = scores.getOrDefault(player.getUniqueId(), 0);
        scores.put(player.getUniqueId(), currentScore + points);
    }

    private boolean matches(String[] result, String input) {
        if (result == null || input == null) return false;

        var word = result[0];
        var reading = result[1];

        // KANJI
        if (word != null && word.equals(input)) {
            return true;
        }

        // KANA
        if (reading != null && reading.equals(input)) {
            return true;
        }

        // ROMAJI
        for (String romaji : Transliterator.transliterate(reading)) {
            if (romaji.equalsIgnoreCase(input)) {
                return true;
            }
        }

        return false;
    }

    public void submitWord(Player sender, String input) {
        String[][] result = new JishoAPI()
                .getFirstSearchResultSimple(input);
        if (result == null || result[0].length == 0 || result[1].length == 0)
            return;

        String[] trimmedResult = new String[] { result[0][0], result[1][0], result[2][0] };

        /// checks if the result is valid and matches the input
        if (!matches(trimmedResult, input))
            return;

        var reading = trimmedResult[1];
        var romaji = Transliterator.transliterate(reading).getFirst();

        ///  Check if the word ends with ん or ン
        if (reading.endsWith("ん") || reading.endsWith("ン")) {
            sendMessage(sender, "word_ends_with_n", msg -> msg.replace("{points}", "" + INCORRECT_POINTS));
            subtractPoints(sender, ShiritoriGame.INCORRECT_POINTS);
            return;
        }

        if (usedWords.contains(reading)) {
            sendMessage(sender, "word_already_used", msg -> msg.replace("{points}", "" + INCORRECT_POINTS));
            subtractPoints(sender, ShiritoriGame.INCORRECT_POINTS);
            return;
        }

        /// check if word starts with last kana
        var lastKana = getLastKana();
        var lastKanaRomaji = Transliterator.transliterate(lastKana).getFirst();

        if (!romaji.startsWith(lastKanaRomaji)) {
            sendMessage(sender, "wrong_kana", msg -> msg
                    .replace("{kana}", lastKana)
                    .replace("{romaji}", lastKanaRomaji)
                    .replace("{points}", "" + INCORRECT_POINTS));
            subtractPoints(sender, ShiritoriGame.INCORRECT_POINTS);
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            /// Passing raw result to get correct dictionary definitions
            player.sendMessage(String.join("\n", List.of(PLAYER_GUESSED_MESSAGE, Common.getDictionaryMessageFromResult(result)))
                    .replace("{player}", sender.getName()));
        }

        sendMessage(sender, "word_correct", msg -> msg.replace("{points}", "" + CORRECT_POINTS));
        addPoints(sender, CORRECT_POINTS);

        usedWords.add(reading);
    }

    private void sendMessage(Player player, String key, Function<String, String> replacer) {
        player.sendMessage(PREFIX + replacer.apply(Messages.getMessage(player, key)));
    }

    public Map<UUID, Integer> getResults() {
        return scores.entrySet()
                .stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));
    }
}
