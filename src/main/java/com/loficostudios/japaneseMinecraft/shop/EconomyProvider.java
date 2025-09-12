package com.loficostudios.japaneseMinecraft.shop;


import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;

public interface EconomyProvider {
    boolean has(PlayerProfile player, double amount);
    boolean withdrawalPlayer(PlayerProfile player, double amount);
    boolean depositPlayer(PlayerProfile player, double amount);
}
