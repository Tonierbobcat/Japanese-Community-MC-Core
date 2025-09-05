package com.loficostudios.japaneseMinecraft.items;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Recipes {
    public static Collection<Recipe> getAll() {
        return List.of(createCraftingRecipe(Items.FLOWER_SWORD, 1, CraftingType.SHAPED, new String[] {" F ", " F ", " S "}, Map.of(
                'F', new ItemStack(Material.POPPY),
                'S', new ItemStack(Material.STICK)
        )));
    }

    /**
     *
     * @throws IllegalArgumentException if item is not registered
     */
    private static Recipe createCraftingRecipe(ItemStack target, int amount, CraftingType type, String[] shape, Map<Character, ItemStack> ingredients) {
        var id = Items.getItemId(target);
        Validate.isTrue(id != null, "Item is not registered");

        target = target.clone();
        target.setAmount(amount);
        var nmk = JapaneseMinecraft.getNMK(id + "_recipe");

        return switch (type) {
            case SHAPED -> {
                var recipe = new ShapedRecipe(nmk, target);
                recipe.shape(shape);
                for (Map.Entry<Character, ItemStack> entry : ingredients.entrySet()) {
                    recipe.setIngredient(entry.getKey(), entry.getValue().getType());
                }
                yield recipe;
            }
            case SHAPELESS -> {
                var recipe = new ShapelessRecipe(nmk, target);
                for (ItemStack ingredient : ingredients.values()) {
                    recipe.addIngredient(ingredient.getType());
                }
                yield recipe;
            }
        };
    }

    private enum CraftingType {SHAPED, SHAPELESS}
}
