package com.loficostudios.japaneseMinecraft.notifications;

public record Notification(String eng, String jp, Type type, double weight) {
    public Notification(String eng, String jp, Type type) {
        this(eng, jp, type, 1);
    }
    public enum Type {
        BOUNTY,
        ANNOUNCEMENT,
        ALERT,
        INFO
    }
}
