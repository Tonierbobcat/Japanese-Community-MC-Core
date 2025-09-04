package com.loficostudios.japaneseMinecraft.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.loficostudios.japaneseMinecraft.Common;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class JishoAPI {
    private static final String API_URL = "https://jisho.org/api/v1/search/words?keyword=";

    public @Nullable JishoSearchResult search(String query) {
        var data = getWordData(query);
        if (data.length == 0) {
            return null;
        }

        var word = data[0];
        var reading = data[1];
        var definition = data[2];

        return new JishoSearchResult(word, reading, definition);
    }

    /**
     * @return [kana, word, definition]
     */
    @SuppressWarnings("unchecked")
    private String[] getWordData(String input) {
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

            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();


            Map<String, Object> map = gson.fromJson(json.toString(), type);
            var data = (List<Object>) map.get("data");
            if (data.isEmpty()) {
                return new String[0];
            }

            var firstEntry = (Map<String, Object>) data.getFirst();
            var japaneseList = (List<Object>) firstEntry.get("japanese");
            if (japaneseList.isEmpty()) {
                return new String[0];
            }
            var firstJapanese = (Map<String, Object>) japaneseList.getFirst();
            if (firstJapanese.isEmpty()) {
                return new String[0];
            }

            String word = (String) firstJapanese.get("word");
            String reading = (String) firstJapanese.get("reading");

            var firstSenses = (List<Object>) firstEntry.get("senses");
            if (firstSenses.isEmpty()) {
                return new String[0];
            }
            var englishDefinitions = (List<String>) ((Map<String, Object>) firstSenses.getFirst()).get("english_definitions");
            if (englishDefinitions.isEmpty()) {
                return new String[0];
            }

            var definition = englishDefinitions.getFirst();

            return new String[]{ word, reading, definition} ;
        } catch (IOException e) {
            Common.notifyAdmins("Error connecting to Jisho API: " + e.getMessage());
            return new String[0];
        }
    }
}
