package com.loficostudios.japaneseMinecraft.util;

import java.util.List;

public class JishoResponse {
    private Meta meta;
    private List<JishoEntry> data;

    public List<JishoEntry> getData() {
        return data;
    }

    public Meta getMeta() {
        return meta;
    }

    public static class Meta {
        private int status;
    }
}
