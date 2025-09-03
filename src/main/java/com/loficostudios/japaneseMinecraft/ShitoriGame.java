package com.loficostudios.japaneseMinecraft;

import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ShitoriGame { // PLACEHOLDER ADD LATER

    private static final String API_URL = "https://jisho.org/api/v1/search/words?keyword=";

    private static String[] words = {
            "neko",      // 猫
            "inu",       // 犬
            "sakura",    // 桜
            "kumo",      // 雲
            "tori",      // 鳥
            "hana",      // 花
            "mizu",      // 水
            "yama",      // 山
            "umi",       // 海
            "ki",        // 木
            "hoshi",     // 星
            "kasa",      // 傘
            "kuruma",    // 車
            "tomodachi", // 友達
            "usagi",     // 兎
            "koinu",     // 子犬
            "ryokou",    // 旅行
            "koi",       // 鯉
            "fune",      // 船
            "tokei",     // 時計
            "kawaii",    // かわいい
            "hikari",    // 光
            "ame",       // 雨
            "denwa",     // 電話
            "yuki"       // 雪
    };

    private final Set<String> usedWords = new HashSet<>();

    public void start() {
        var word = words[(int) (Math.random() * words.length)];

        List<String> strings = List.of(new String[] {
                "しりとりゲームを始めます！最初の言葉は「{word}」です。",
                "Let's start a Shitori game! The first word is '{word}'."
        });

        Common.notifyPlayers(String.join("\n", strings).replace("{word}", word));;
    }

    private String getLastKana() {
        var current = usedWords.stream().reduce((first, second) -> second).orElse("");
        return ""; // placeholder
    }

    private void submitWord(Player sender, String word) {
        if (usedWords.contains(word.toLowerCase())) {
            sender.sendMessage(Messages.getMessage(sender, "word_used"));
            return;
        }

        var data = getWordData(word);
        var valid = data.length == 3;
        if (!valid) {
            sender.sendMessage(Messages.getMessage(sender, "word_invalid"));
            return;
        }

        var romaji = data[0];
        var kana = data[1];
        var japanese = data[2];

        if (romaji.endsWith("n")) {
            sender.sendMessage(Messages.getMessage(sender, "word_invalid"));
            return;
        }

        // check if word starts with last kana
        var lastKana = getLastKana();
        if (romaji.startsWith(lastKana.toLowerCase())) {
            sender.sendMessage(Messages.getMessage(sender, "word_invalid"));
            return;
        }

        usedWords.add(word.toLowerCase());
    }

    /*
        {
            "data": [
            {
              "slug": "単語",
              "is_common": true,
              "tags": [
                "wanikani15"
              ],
              "jlpt": [
                "jlpt-n3"
              ],
              "japanese": [
                {
                  "word": "単語",
                  "reading": "たんご"
                }
              ],
              "senses": [
                {
                  "english_definitions": [
                    "word",
                    "vocabulary"
                  ],
                  "parts_of_speech": [
                    "Noun"
                  ],
                  "links": [],
                  "tags": [],
                  "restrictions": [],
                  "see_also": [],
                  "antonyms": [],
                  "source": [],
                  "info": []
                }
              ],
              "attribution": {
                "jmdict": true,
                "jmnedict": false,
                "dbpedia": false
              }
            }]
        }
     */
    public String[] getWordData(String input) {
        try {
            URL url = new URL(API_URL + input);
            HttpURLConnection connection = ((HttpURLConnection) url.openConnection());
            connection.setRequestMethod("GET");
            connection.connect();

            int response = connection.getResponseCode();

            if (response != 200) {
                Common.notifyAdmins("Jisho API returned non-200 response: " + response);
                return new String[0];
            }

            StringBuilder json = new StringBuilder();
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                json.append(scanner.nextLine());
            }
            scanner.close();

            Debug.log("Jisho API response: " + json);
            return new String[0];
        } catch (IOException e) {
            Common.notifyAdmins("Error connecting to Jisho API: " + e.getMessage());
            return new String[0];
        }
    }
}
