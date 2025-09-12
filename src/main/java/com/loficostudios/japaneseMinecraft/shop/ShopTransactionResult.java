package com.loficostudios.japaneseMinecraft.shop;

public record ShopTransactionResult<Impl extends ShopItem<Impl>>(ShopInstance<Impl> instance, Integer amount, Type type) {
    public ShopTransactionResult(ShopInstance<Impl> instance, Type type) {
        this(instance, 1, type);
    }
    public enum Type {
        SUCCESS,
        FAILURE,
        UNKNOWN,
        NO_PERMISSION,
        NOT_ENOUGH_MONEY,
        NO_INVENTORY_SPACE
    }
}
