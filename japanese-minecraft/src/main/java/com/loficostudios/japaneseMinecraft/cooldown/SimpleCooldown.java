/**
 * @Author Logan
 * @Github https://github.com/Tonierbobcat
 */

package com.loficostudios.japaneseMinecraft.cooldown;

import java.util.UUID;

public class SimpleCooldown extends ComplexCooldown {
    public SimpleCooldown(long intervalMillis) {
        super(intervalMillis);
    }
    
    @Override
    public boolean has(UUID uuid) {
        var has = super.has(uuid);
        if (!has)
            set(uuid);
        return has;
    }
}
