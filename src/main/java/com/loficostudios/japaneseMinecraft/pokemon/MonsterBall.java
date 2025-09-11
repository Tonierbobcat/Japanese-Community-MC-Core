package com.loficostudios.japaneseMinecraft.pokemon;

import com.loficostudios.forgified.paper.items.JItem;
import org.bukkit.Material;
import org.jetbrains.annotations.Range;

public class MonsterBall extends JItem {
    private final double capturePower;
    public MonsterBall(@Range(from = 0, to = 10) double capturePower, Properties properties) {
        super(Material.SNOWBALL, properties
                .stackTo(64));
        this.capturePower = capturePower;
    }

    public double getCapturePower() {
        return capturePower;
    }
}
