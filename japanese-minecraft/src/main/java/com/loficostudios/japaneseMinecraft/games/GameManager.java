package com.loficostudios.japaneseMinecraft.games;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameManager {
    private static final int AUTO_START_MINUTES = 25;

    private final List<Game> games;
    private Game currentGame;

    public GameManager(List<Game> games) {
        this.games = games;
        JapaneseMinecraft.runTaskTimer(this::startRandomGame, 20 * 60, 20 * 60 * AUTO_START_MINUTES);
    }

    public boolean isGameRunning() {
        return this.currentGame != null && currentGame.isActive();
    }

    public GameStartResult startGame(String id) {
        for (Game game : games) {
            if (game.getId().equals(id)) {
                return startGame(game);
            }
        }
        return GameStartResult.INVALID_GAME;
    }

    public GameStartResult startGame(Game game) {
        if (isGameRunning())
            return GameStartResult.GAME_ALREADY_RUNNING;
        if (Bukkit.getOnlinePlayers().size() < game.getMinPlayers())
            return GameStartResult.NOT_ENOUGH_PLAYERS;
        this.currentGame = game;
        game.reset();
        game.start();

        JapaneseMinecraft.runTaskTimer(new BukkitRunnable() {
            long elapsedTicks;
            @Override
            public void run() {
                if (!isGameRunning()) {
                    stop();
                    this.cancel();
                    return;
                }

                elapsedTicks++;
                long gameLengthTicks = game.getLengthMinutes() * 60L * 20L;
                if (elapsedTicks >= gameLengthTicks) {
                    stop();
                    this.cancel();
                }
            }
        }, 1, 1);
        return GameStartResult.SUCCESS;
    }

    /// Will try to start a new game will return early if requirements are not meant
    public void startRandomGame() {
        if (games.isEmpty())
            return;
        var index = ThreadLocalRandom.current().nextInt(games.size());

        var game = games.get(index);
        if (game == null)
            return;
        startGame(game);
    }

    public void stop() {
        if (!isGameRunning())
            return;
        var tmp = this.currentGame;
        this.currentGame = null;
        tmp.end();
    }

    public enum GameStartResult {
        NOT_ENOUGH_PLAYERS,
        INVALID_GAME, SUCCESS, GAME_ALREADY_RUNNING;
        public boolean isSuccess() {
            return this.equals(SUCCESS);
        }
    }
}
