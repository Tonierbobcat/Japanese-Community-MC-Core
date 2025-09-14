package com.loficostudios.japaneseMinecraft.games.shiritori;

import com.github.jikyo.romaji.Transliterator;
import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.games.Game;
import com.loficostudios.japaneseMinecraft.util.JishoAPI;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ShiritoriGame implements Game, Listener { // PLACEHOLDER ADD LATER

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

    public static final String PREFIX = Common.createMessagePrefix("Shiritori", "§d");

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

    private boolean active;

    @Override
    public void reset() {
        usedWords.clear();
        scores.clear();
    }

    @Override
    public void start() {
        active = true;
        var word = DEFAULT_WORDS[ThreadLocalRandom.current().nextInt(DEFAULT_WORDS.length)];
        usedWords.add(word);

        Common.notifyPlayers(STARTING_MESSAGE.replace("{word}", word).replace("{minutes}", "" + gameLengthMinutes));
    }

    @Override
    public int getMinPlayers() {
        return 2;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String getId() {
        return "shiritori";
    }

    /// Get the last kana of the last used word
    private String getLastKana() { // todo this method needs to be double checked by somebody more literate in japanese
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

    /// Methods for adding and subtracting points.

    private void subtractPoints(Player player, int points) {
        var currentScore = scores.getOrDefault(player.getUniqueId(), 0);
        scores.put(player.getUniqueId(), Math.max(0, currentScore - points));
    }

    private void addPoints(Player player, int points) {
        var currentScore = scores.getOrDefault(player.getUniqueId(), 0);
        scores.put(player.getUniqueId(), currentScore + points);
    }

    /*
    TODO this method needs to be fixed. This needs to pass in the JishoAPI Response and then if the players input matches
    the word then return the String[{word[], reading[], definition[]}]
    */
    /// Practically checks if the player typed in the correct word
    /// Although the jisho api may pull up results. the input must match exactly in order for it to be a valid guess
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
            notifyPlayer(sender, "word_ends_with_n", msg -> msg.replace("{points}", "" + INCORRECT_POINTS));
            subtractPoints(sender, ShiritoriGame.INCORRECT_POINTS);
            return;
        }

        if (usedWords.contains(reading)) {
            notifyPlayer(sender, "word_already_used", msg -> msg.replace("{points}", "" + INCORRECT_POINTS));
            subtractPoints(sender, ShiritoriGame.INCORRECT_POINTS);
            return;
        }

        /// check if word starts with last kana
        var lastKana = getLastKana();
        var lastKanaRomaji = Transliterator.transliterate(lastKana).getFirst();

        if (!romaji.startsWith(lastKanaRomaji)) {
            notifyPlayer(sender, "wrong_kana", msg -> msg
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

        notifyPlayer(sender, "word_correct", msg -> msg.replace("{points}", "" + CORRECT_POINTS));
        addPoints(sender, CORRECT_POINTS);

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

    @Override
    public int getLengthMinutes() {
        return 2;
    }

    @Override
    public void end() {
        active = false;
        var results = getResults();

        var message = getResultsMessage(results);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message.replace("{points}", "" + results.getOrDefault(player.getUniqueId(), 0)));
        }

        int fifthPlaceMoney = 100;
        int fourthPlaceMoney = 100;
        int thirdPlaceMoney = 100;
        int secondPlaceMoney = 300;
        int firstPlaceMoney = 750;

        Map<Integer, Integer> moneyRewards = Map.of(
                0, firstPlaceMoney,
                1, secondPlaceMoney,
                2, thirdPlaceMoney,
                3, fourthPlaceMoney,
                4, fifthPlaceMoney
        );

        int index = 0;
        for (Map.Entry<UUID, Integer> entry : results.entrySet()) {
            var reward = moneyRewards.get(index);
            if (reward != null) {
                var eng = ShiritoriGame.PREFIX + "You have received a ${money} reward!"
                        .replace("{money}", "" + reward);
                var player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    /// Add money reward to player
                    JapaneseMinecraft.getPlayerProfile(player)
                            .addMoney(reward);
                    player.sendMessage(eng);
                }
            }
            index++;
        }
    }

    private String getResultsMessage(Map<UUID, Integer> results) {
        int playersToDisplay = 5;
        int index = 0;
        List<String> lines = new ArrayList<>();
        lines.add("§a§l=== Shiritori Results ===");
        for (Map.Entry<UUID, Integer> entry : results.entrySet()) {
            var player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline())
                continue;
            if (index < playersToDisplay) {
                lines.add("§e{rank}. §f{name} §6- §e{points} point(s)"
                        .replace("{rank}", "" + (index + 1))
                        .replace("{name}", player.getName())
                        .replace("{points}", "" + entry.getValue()));
                index++;
            } else {
                break;
            }
        }
        lines.add("§6You scored §e{points}§6 point(s)!");
        lines.add("§a§l=====================");

        return String.join("\n", lines);
    }

    @EventHandler
    private void onChat(AsyncChatEvent e) {
        if (!isActive())
            return;

        /// run it off the thread that called the event
        JapaneseMinecraft.runTaskAsynchronously(() -> {
            var message = PlainTextComponentSerializer.plainText().serialize(e.message());
            submitWord(e.getPlayer(), message);
        });
    }
}
