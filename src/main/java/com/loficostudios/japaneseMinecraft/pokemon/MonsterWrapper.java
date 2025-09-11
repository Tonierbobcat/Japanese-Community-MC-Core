package com.loficostudios.japaneseMinecraft.pokemon;

import com.loficostudios.japaneseMinecraft.Items;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.apache.commons.lang3.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class MonsterWrapper {

    /// We use this NSK to store the ball id so when the ball gets thrown out it will drop it again
    private final NamespacedKey ballKey;

    private final NamespacedKey ownerKey;
    private final NamespacedKey originalOwnerKey;

    private final NamespacedKey leveledEntityKey;
    private final NamespacedKey dateCaughtKey;

    private final LivingEntity entity;

    private final PersistentDataContainer pdc;

    public MonsterWrapper(JapaneseMinecraft plugin, LivingEntity entity) {
        this.entity = entity;

        this.pdc = entity.getPersistentDataContainer();

        /// hehe 'balls'
        ballKey = new NamespacedKey(plugin, "balls");

        leveledEntityKey = new NamespacedKey(plugin, "leveledEntity");
        ownerKey = new NamespacedKey(plugin, "ownerKey");
        originalOwnerKey = new NamespacedKey(plugin, "originalOwnerKey");
        dateCaughtKey = new NamespacedKey(plugin, "dateCaught");
    }

    public @Nullable MonsterBall getMonsterBall() {
        var id = pdc.get(ballKey, PersistentDataType.STRING);
        var item = Items.ITEMS.getById(id);
        return item instanceof MonsterBall ? (((MonsterBall) item)) : null;
    }

    public void setMonsterBall(MonsterBall ball) {
        Validate.isTrue(Items.ITEMS.getById(ball.getId()) != null, "Monster Ball is not registered");
        pdc.set(ballKey, PersistentDataType.STRING, ball.getId());
    }

    public boolean isWild() {
        /// No need to check original owner
        return getOwner() == null;
    }

    public @Nullable LivingEntity getEntity() {
        return entity;
    }

    public @Nullable Long getDateCaught() {
        return pdc.get(dateCaughtKey, PersistentDataType.LONG);
    }

    public void setDateCaught(long date) {
        pdc.set(dateCaughtKey, PersistentDataType.LONG, date);
    }

    public void setOwner(Player whoCaught) {
        pdc.set(ownerKey, PersistentDataType.STRING, whoCaught.getName());
    }

    public void setOriginalOwner(Player whoCaught) {
        pdc.set(originalOwnerKey, PersistentDataType.STRING, whoCaught.getName());
    }

    public @Nullable String getOriginalOwner() {
        return pdc.get(originalOwnerKey, PersistentDataType.STRING);
    }

    public @Nullable String getOwner() {
        return pdc.get(ownerKey, PersistentDataType.STRING);
    }

    public void setLevel(int level) {
        pdc.set(leveledEntityKey, PersistentDataType.INTEGER, level);
    }

    public  @Nullable Integer getLevel() {
        return pdc.get(leveledEntityKey, PersistentDataType.INTEGER);
    }

    public boolean isCurrentOwner(Player player) {
        return getOwner() != null && getOwner().equals(player.getName());
    }
}
