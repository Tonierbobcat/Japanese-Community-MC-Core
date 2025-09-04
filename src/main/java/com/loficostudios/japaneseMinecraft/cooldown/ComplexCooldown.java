/**
 * @Author Logan
 * @Github https://github.com/Tonierbobcat
 */

package com.loficostudios.japaneseMinecraft.cooldown;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ComplexCooldown implements Cooldown {
    private final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final long interval;
    public ComplexCooldown(long intervalMillis) {
        this.interval = intervalMillis;
    }


    @Override
    public boolean has(UUID uuid) {
        Long last = cooldowns.get(uuid);
        if (last == null) {
            return false;
        }
        if ((System.currentTimeMillis() - last) >= interval) {
            cooldowns.remove(uuid);
            return false;
        }
        return true;
    }

    @Override
    public void set(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis());
    }
}
