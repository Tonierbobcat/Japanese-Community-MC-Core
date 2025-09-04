package com.loficostudios.japaneseMinecraft.games.shiritori;

import com.github.jikyo.romaji.Transliterator;
import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.Debug;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.Messages;
import com.loficostudios.japaneseMinecraft.util.JishoAPI;
import org.bukkit.entity.Player;

import java.util.*;

public class ShiritoriGame { // PLACEHOLDER ADD LATER

    private static String[] words = {
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

    /// Stores the japanese reading of the used words
    private final Set<String> usedWords = new LinkedHashSet<>();

    private final int gameLengthMinutes;

    private final Map<UUID, Integer> scores = new HashMap<>();

    public ShiritoriGame(int gameLengthMinutes) {
        this.gameLengthMinutes = gameLengthMinutes;
    }


    public void start() {
        var word = words[(int) (Math.random() * words.length)];

        List<String> strings = List.of(new String[] {
                "Shiritori is starting!",
                "The game will last for {minutes} minute(s).",
                "The player with the most points at the end wins!",
                "The first word is '{word}'."
        });

        Common.notifyPlayers(String.join("\n", strings).replace("{word}", word).replace("{minutes}", "" + gameLengthMinutes));
        usedWords.add(word);
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

    public void submitWord(Player sender, String word) {
        var result = new JishoAPI().search(word);
        if (result == null) {
            sender.sendMessage(Messages.getMessage(sender, "word_invalid"));
            return;
        }
        var reading = result.reading();
        var romaji = Transliterator.transliterate(reading).getFirst();

        Debug.log("Result: " + result);

        ///  Check if the word ends with ん or ン
        if (reading.endsWith("ん") || reading.endsWith("ン")) {
            var eng = "You cannot use a word that ends with 'ん'. You lost 1 point!";
            var jp = "「ん」で終わる言葉は使えません。1ポイント失います！";
            sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender)  ? jp : eng);
            subtractPoints(sender, 1);
            return;
        }

        if (usedWords.contains(reading)) {
            var eng = "This word has already been used. Please try another word. You lost 1 point!";
            var jp = "この言葉はすでに使われています。別の言葉を試してください。1ポイント失います！";
            sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
            subtractPoints(sender, 1);
            return;
        }

        /// check if word starts with last kana
        var lastKana = getLastKana();
        var lastKanaRomaji = Transliterator.transliterate(lastKana).getFirst();

        Debug.log("UsedWords: " + usedWords + " Romaji: " + romaji + ", Last Kana: " + lastKana + ", Last Kana Romaji: " + lastKanaRomaji);
        if (!romaji.startsWith(lastKanaRomaji)) {
            var eng = "Your word must start with '" + lastKana + "' (romaji: " + lastKanaRomaji + "). You lost 1 point!";
            var jp = "あなたの言葉は'" + lastKana + "'（ローマ字: " + lastKanaRomaji + "）で始まらなければなりません。1ポイント失います！";
            sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
            subtractPoints(sender, 1);
            return;
        }

        var definition = result.definition();
        var list = List.of(
                result.word() != null ? "Word: " + result.word() + " (" + reading + ")" : "Word: " + reading,
                "Definition: " + definition
        );

        Common.notifyPlayers(sender.getName() + " gets a point!\n" + String.join("\n", list));
        var eng = "You get 2 points!";
        var jp = "2ポイント獲得！";
        sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
        addPoints(sender, 2);

        Debug.log("Added word: " + reading);
        usedWords.add(reading);
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
