package com.loficostudios.japaneseMinecraft.util;

import com.google.gson.Gson;
import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.Debug;
import org.jetbrains.annotations.Nullable;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JishoAPI {
    private static final String API_URL = "https://jisho.org/api/v1/search/words?keyword=";

    private final Gson gson = new Gson();

    public @Nullable JishoResponse search(String query) {
        Debug.log("JishoAPI Query: " + query);
        try {
            URL url = new URL(API_URL + query.replace(" ", "+"));
            HttpURLConnection connection = ((HttpURLConnection) url.openConnection());
            connection.setRequestMethod("GET");
            connection.connect();

            int response = connection.getResponseCode();

            if (response != 200) {
                Debug.log("JishoAPI non-200 response: " + response);
                Common.notifyAdmins("Jisho API returned non-200 response: " + response);
                return null;
            }

            StringBuilder json = new StringBuilder();
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                json.append(scanner.nextLine());
            }
            scanner.close();

            var result = gson.fromJson(json.toString(), JishoResponse.class);
            Debug.log("JishoAPI Response: " + (result != null ? result.toString() : "null"));
            return result;
        } catch (Exception e) {
            Debug.log("Error connecting to Jisho API: " + e.getMessage());
            Common.notifyAdmins("Error connecting to Jisho API: " + e.getMessage());
            return null;
        }
    }

    /**
     *
     * @return String array of size 2: [word, reading, definitions] or null if not found
     */
    public String[][] getFirstSearchResultSimple(String query) {
        var response = search(query);

        if (response == null || response.getData().isEmpty()) return null;

        var data = response.getData().getFirst();

        List<String> words = new ArrayList<>();
        List<String> readings = new ArrayList<>();
        for (JishoEntry.Japanese japanese : data.getJapanese()) {
            words.add(japanese.getWord() != null ? japanese.getWord() : "null");
            readings.add(japanese.getReading() != null ? japanese.getReading() : "null");
        }

        List<String> definitions = new ArrayList<>();
        for (JishoEntry.Sense sense : data.getSenses()) {
            definitions.addAll(sense.getEnglishDefinitions());
        }

        return new String[][]{words.toArray(String[]::new), readings.toArray(String[]::new), definitions.toArray(String[]::new)};
    }
}
