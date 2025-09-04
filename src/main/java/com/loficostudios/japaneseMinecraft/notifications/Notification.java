package com.loficostudios.japaneseMinecraft.notifications;

public record Notification(String eng, String jp, Type type) {
    public enum Type {
        BOUNTY,
        ANNOUNCEMENT,
        ALERT,
        INFO
    }
}
