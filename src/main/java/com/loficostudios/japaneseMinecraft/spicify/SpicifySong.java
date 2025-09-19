package com.loficostudios.japaneseMinecraft.spicify;

public record SpicifySong(SpicifyService service, int id, String title) {
    public int likes() {
        return service.getLikes(this);
    }
}