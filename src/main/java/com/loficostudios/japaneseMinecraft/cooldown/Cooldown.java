/**
 * @Author Logan
 * @Github https://github.com/Tonierbobcat
 */

package com.loficostudios.japaneseMinecraft.cooldown;

import java.util.UUID;

public interface Cooldown {

    /**
     * @return {@code true} if {@code UUID} has cooldown
     */
    boolean has(UUID uuid);

    void set(UUID uuid);

}
