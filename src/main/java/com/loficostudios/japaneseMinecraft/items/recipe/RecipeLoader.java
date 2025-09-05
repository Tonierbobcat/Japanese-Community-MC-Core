package com.loficostudios.japaneseMinecraft.items.recipe;

import com.loficostudios.japaneseMinecraft.util.IPluginResources;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/// This is a functional interface that loads a recipe from Map<String, Object>

// TODO finish recipe loading system

/// I want to load the recipes folder from resources and the iterate recursively through all files in it and check what type of recipe it is
/// and then get the appropriate RecipeLoader to load it
/// for example if it's a shapeless recipe, use ShapelessRecipeLoader to load it
/// then run Bukkit.addRecipe() to add it to the server

/// You can create custom recipe loaders

public interface RecipeLoader<T> {
    @Nullable T load(IPluginResources resources, Map<String, Object> data);

//    static @Nullable ItemStack getFromId(String id) {
//        var split = id.split(":");
//        if (split.length != 2) {
//            return getFromNonNamespacedId(id);
//        } else {
//            /// This is much faster than
//            var namespace = split[0];
//            var key = split[1];
//
//            if (namespace.equals("minecraft")) {
//                try {
//                    return new ItemStack(Material.valueOf(key.toUpperCase()));
//                } catch (IllegalArgumentException e) {
//                    return null;
//                }
//                /// If the name space is this plugins
//            } else if (namespace.equals(namespace())) {
//                var jitem = Items.ITEMS.getById(id);
//                return jitem != null ? jitem.getItemStack(1) : null;
//                /// Else parse non-namespaced id
//            } else {
//                return getFromNonNamespacedId(id);
//            }
//        }
//    }
//
//    static @Nullable ItemStack getFromNonNamespacedId(String id) {
//        var jitem = Items.ITEMS.getById(id);
//        if (jitem != null) {
//            return jitem.getItemStack(1);
//        } else {
//            try {
//                return new ItemStack(Material.valueOf(id));
//            } catch (IllegalArgumentException e) {
//                return null;
//            }
//        }
//    }
}
