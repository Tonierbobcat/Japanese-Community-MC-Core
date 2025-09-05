package com.loficostudios.japaneseMinecraft.items.recipe;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.loficostudios.japaneseMinecraft.items.recipe.impl.BlastingRecipeLoader;
import com.loficostudios.japaneseMinecraft.items.recipe.impl.CampfireCookingRecipeLoader;
import com.loficostudios.japaneseMinecraft.items.recipe.impl.ShapedRecipeLoader;
import com.loficostudios.japaneseMinecraft.items.recipe.impl.ShapelessRecipeLoader;
import com.loficostudios.japaneseMinecraft.util.FileUtils;
import com.loficostudios.japaneseMinecraft.util.IPluginResources;
import org.bukkit.inventory.Recipe;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.function.BiConsumer;

public class RecipeLoaderRegistry<T> {
    private Map<RecipeType, RecipeLoader<T>> loaders;

    private BiConsumer<IPluginResources, T> onLoad;

    public RecipeLoaderRegistry(Map<RecipeType, RecipeLoader<T>> loaders, BiConsumer<IPluginResources, T> onLoad) {
        this.loaders = loaders;
        this.onLoad = onLoad;
    }

    public void initialize(IPluginResources resources) {
        FileUtils.extractDataFolderAndUpdate(resources, "recipes", (file) -> {
            Map<String, Object> data;
            try {
                data = new Gson().fromJson(
                        new FileReader(file), new TypeToken<Map<String, Object>>() {}.getType()
                );
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            String typeString = (String) data.get("type");
            if (typeString == null) {
                throw new IllegalArgumentException("Recipe file missing 'type': " + file.getName());
            }

            RecipeType type = RecipeType.fromString(typeString);

            RecipeLoader<T> loader = loaders.get(type);
            if (loader == null) {
                throw new IllegalArgumentException("No loader registered for type: " + type);
            }

            T recipe = loader.load(resources, data);
            if (recipe == null) {
                throw new IllegalArgumentException("Failed to load recipe: " + file.getName());
            }

            onLoad.accept(resources, recipe);
        });
    }

    public static RecipeLoaderRegistry<Recipe> getBukkitRecipeLoaderRegistry() {
        return new RecipeLoaderRegistry<>(Map.of(
                RecipeType.CRAFTING_SHAPED, new ShapedRecipeLoader(),
                RecipeType.CRAFTING_SHAPELESS, new ShapelessRecipeLoader(),
                RecipeType.CAMPFIRE_COOKING, new CampfireCookingRecipeLoader(),
                RecipeType.BLASTING, new BlastingRecipeLoader(),
                RecipeType.SMELTING, new BlastingRecipeLoader(),
                RecipeType.SMOKING, new BlastingRecipeLoader()
        ), (resources, recipe) -> resources.getServer().addRecipe(recipe));
    }
}
