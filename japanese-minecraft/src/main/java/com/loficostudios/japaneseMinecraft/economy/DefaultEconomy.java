package com.loficostudios.japaneseMinecraft.economy;

import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;
import com.loficostudios.japaneseMinecraft.shop.EconomyProvider;

public class DefaultEconomy implements EconomyProvider {

    @Override
    public boolean has(PlayerProfile player, double amount) {
        return player.hasMoney(((int) Math.round(amount)));
    }

    @Override
    public double getBalance(PlayerProfile player) {
        return player.getMoney();
    }

    @Override
    public boolean withdrawPlayer(PlayerProfile player, double amount) {
        player.subtractMoney(amount);
        return true;
    }

    @Override
    public boolean depositPlayer(PlayerProfile player, double amount) {
        player.addMoney(amount);
        return true;
    }
}
