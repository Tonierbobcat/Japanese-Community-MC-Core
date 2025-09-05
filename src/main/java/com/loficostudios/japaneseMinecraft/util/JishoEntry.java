package com.loficostudios.japaneseMinecraft.util;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JishoEntry {

    private String slug;
    private Boolean isCommon;
    private List<String> tags;
    private List<String> jlpt;
    private List<Japanese> japanese;
    private List<Sense> senses;
    private Attribution attribution;

    public String getSlug() {
        return slug;
    }

    public Boolean getCommon() {
        return isCommon;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getJlpt() {
        return jlpt;
    }

    public List<Japanese> getJapanese() {
        return japanese;
    }

    public List<Sense> getSenses() {
        return senses;
    }

    public Attribution getAttribution() {
        return attribution;
    }

    public static class Japanese {
        private String word;
        private String reading;

        public String getWord() {
            return word;
        }

        public String getReading() {
            return reading;
        }
    }

    public static class Sense {
        @SerializedName("english_definitions")
        private List<String> englishDefinitions;

        @SerializedName("parts_of_speech")
        private List<String> partsOfSpeech;

        private List<Link> links;
        private List<String> tags;
        private List<String> restrictions;

        @SerializedName("see_also")
        private List<String> seeAlso;

        private List<String> antonyms;
        private List<Object> source;
        private List<Object> info;
        private List<String> sentences;

        public List<String> getEnglishDefinitions() {
            return englishDefinitions;
        }

        public List<String> getPartsOfSpeech() {
            return partsOfSpeech;
        }

        public List<Link> getLinks() {
            return links;
        }

        public List<String> getTags() {
            return tags;
        }

        public List<String> getRestrictions() {
            return restrictions;
        }

        public List<String> getSeeAlso() {
            return seeAlso;
        }

        public List<String> getAntonyms() {
            return antonyms;
        }

        public List<Object> getSource() {
            return source;
        }

        public List<Object> getInfo() {
            return info;
        }

        public List<String> getSentences() {
            return sentences;
        }
    }

    public static class Link {
        private String text;
        private String url;

        public String getText() {
            return text;
        }

        public String getUrl() {
            return url;
        }
    }

    public static class Attribution {
        private Boolean jmdict;
        private Boolean jmnedict;

        /// Can be boolean or String
        private Object dbpedia;

        public Boolean getJmdict() {
            return jmdict;
        }

        public Boolean getJmnedict() {
            return jmnedict;
        }

        public Object getDbpedia() {
            return dbpedia;
        }
    }
}
