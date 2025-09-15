package com.loficostudios.japaneseMinecraft;

import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;
import com.loficostudios.japaneseMinecraft.shop.EconomyProvider;
import org.apache.commons.lang3.Validate;

public class Economy implements EconomyProvider {

    @Override
    public boolean has(PlayerProfile player, double amount) {
        return player.hasMoney(((int) Math.round(amount)));
    }

    @Override
    public boolean withdrawalPlayer(PlayerProfile player, double amount) {
        player.subtractMoney(amount);
        return true;
    }

    @Override
    public boolean depositPlayer(PlayerProfile player, double amount) {
        player.addMoney(amount);
        return true;
    }
}
