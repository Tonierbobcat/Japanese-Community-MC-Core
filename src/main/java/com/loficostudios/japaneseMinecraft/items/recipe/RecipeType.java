package com.loficostudios.japaneseMinecraft.items.recipe;

/// Source: [Datapack Wiki](https://datapack.wiki/wiki/files/recipes)
public enum RecipeType {

    /// A recipe for a blast furnace
    BLASTING("minecraft:blasting"),

    /// A recipe for cooking items on a campfire
    CAMPFIRE_COOKING("minecraft:campfire_cooking"),

    /// A shaped crafting recipe in a crafting table. Ingredients must be placed in the correct pattern to craft
    CRAFTING_SHAPED("minecraft:crafting_shaped"),

    /// A shapeless recipe in a crafting table. Ingredients can be placed in any pattern to craft
    CRAFTING_SHAPELESS("minecraft:crafting_shapeless"),

    /// A recipe for a furnace
    SMELTING("minecraft:smelting"),

    /// A recipe for a smoker
    SMOKING("minecraft:smoking");

    private final String type;

    RecipeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static RecipeType fromString(String type) {
        for (RecipeType rt : RecipeType.values()) {
            if (rt.type.equalsIgnoreCase(type)) {
                return rt;
            }
        }
        return null;
    }
}