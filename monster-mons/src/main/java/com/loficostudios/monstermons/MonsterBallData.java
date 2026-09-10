package com.loficostudios.monstermons;

import com.loficostudios.forgified.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public record MonsterBallData(String ballId, @Nullable MonsterData monsterData) implements Serializable {
    public MonsterBall getBall() {
        return Registries.ITEM.getById(ballId, MonsterBall.class);
    }
}
