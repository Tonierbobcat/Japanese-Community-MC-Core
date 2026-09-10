package com.loficostudios.japaneseMinecraft.shop;

import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;

public interface ShopItem<Impl extends ShopItem<Impl>> {
    ShopTransactionResult<Impl> onBuy(ShopInstance<Impl> instance, int amount, PlayerProfile player);

    String getName();
}
