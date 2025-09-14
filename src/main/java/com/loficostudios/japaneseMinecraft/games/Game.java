package com.loficostudios.japaneseMinecraft.games;

public interface Game {

    int getLengthMinutes();

    void reset();

    void end();

    void start();

    int getMinPlayers();

    boolean isActive();

    String getId();
}
