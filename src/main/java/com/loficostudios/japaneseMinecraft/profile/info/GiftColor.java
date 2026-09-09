package com.loficostudios.japaneseMinecraft.profile.info;

import org.bukkit.Color;
import org.jetbrains.annotations.Nullable;

public enum GiftColor {
    WHITE("#FFFFFF"),
    BLACK("#000000"),
    RED("#FF0000"),
    GREEN("#008000"),
    BLUE("#0000FF"),
    YELLOW("#FFFF00"),
    CYAN("#00FFFF"),
    MAGENTA("#FF00FF"),
    ORANGE("#FFA500"),
    PURPLE("#800080"),
    PINK("#FFC0CB"),
    BROWN("#A52A2A"),
    GRAY("#808080"),
    LIGHT_GRAY("#D3D3D3"),
    DARK_GRAY("#A9A9A9"),
    LIME("#00FF00"),
    NAVY("#000080"),
    TEAL("#008080"),
    OLIVE("#808000"),
    MAROON("#800000"),
    SILVER("#C0C0C0"),
    GOLD("#FFD700"),
    CORAL("#FF7F50"),
    INDIGO("#4B0082"),
    VIOLET("#EE82EE"),
    SALMON("#FA8072"),
    KHAKI("#F0E68C"),
    TURQUOISE("#40E0D0"),
    CHOCOLATE("#D2691E"),
    CRIMSON("#DC143C");

    private final String hex;

    GiftColor(String hex) {
        this.hex = hex;
    }

    public String hex() {
        return hex;
    }

    public int asRGB() {
        return Integer.parseInt(hex().replace("#", ""), 16);
    }

    public Color bukkit() {
        return Color.fromRGB(asRGB());
    }
}
