package com.loficostudios.japaneseMinecraft.pokemon;

import com.loficostudios.forgified.paper.items.JItem;
import org.bukkit.Material;

public class MonsterBall extends JItem {
    private final int capturePower;
    public MonsterBall(int capturePower, Properties properties) {
        super(Material.SNOWBALL, properties
                .stackTo(64));
        this.capturePower = capturePower;
    }

    public int getCapturePower() {
        return capturePower;
    }
}
