package com.loficostudios.monstermons;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.UUID;

public class MonsterData implements Serializable {
    private @Nullable UUID ownerUID;
    private @Nullable UUID originalOwnerUID;
    private final String name;
    private final EntityType base;
    private long dateCaught;
    private int level;
    private final MonsterGender gender;

    public MonsterData(
            @Nullable UUID ownerUID,
            @Nullable UUID originalOwnerUID,
            String name,
            EntityType base,
            long dateCaught,
            int level, MonsterGender gender
    ) {
        this.ownerUID = ownerUID;
        this.originalOwnerUID = originalOwnerUID;
        this.name = name;
        this.base = base;
        this.dateCaught = dateCaught;
        this.level = level;
        this.gender = gender;
    }

    public MonsterGender getGender() {
        return gender;
    }

    public @Nullable UUID ownerUID() {
        return ownerUID;
    }

    public @Nullable UUID originalOwnerUID() {
        return originalOwnerUID;
    }

    public boolean isWild() {
        return originalOwnerUID == null && ownerUID == null;
    }

    public String name() {
        return name;
    }

    public EntityType base() {
        return base;
    }

    public long dateCaught() {
        return dateCaught;
    }

    public int level() {
        return level;
    }

    public void level(int level) {
        this.level =level;
    }

    public @Nullable OfflinePlayer owner() {
        if (ownerUID == null)
            return null;

        return Bukkit.getOfflinePlayer(ownerUID);
    }

    public void originalOwner(OfflinePlayer originalOwner) {
        this.originalOwnerUID = originalOwner.getUniqueId();
    }

    public void owner(Player whoCaught) {
        this.ownerUID = whoCaught.getUniqueId();
    }

    public void dataCaught(long date) {
        this.dateCaught = date;
    }

    public boolean isOwner(Player player) {
        return ownerUID != null && ownerUID.equals(player.getUniqueId());
    }
}