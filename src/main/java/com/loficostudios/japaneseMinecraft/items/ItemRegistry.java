package com.loficostudios.japaneseMinecraft.items;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class ItemRegistry {

    private static NamespacedKey itemKey;

    private final Map<String, JItem> registered = new HashMap<>();

    private Map<String, Map<String, Object>> itemsMap;

    public void initialize(JapaneseMinecraft plugin) {
        itemKey = new NamespacedKey(plugin, "items");

        File json = new File(plugin.getDataFolder(), "items.json");

        /// Always save a fresh copy of the items.json to ensure its up to date
        plugin.saveResource("items.json", true);

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();

        try (FileReader reader = new FileReader(json)) {
            itemsMap = gson.fromJson(reader, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read items.json", e);
        }
    }


    public JItem create(String id, Consumer<ItemStack> item) {
        Validate.isTrue(!registered.containsKey(id), "Item with id " + id + " is already registered");
        var i = new JItem(id, (jitem) -> {
            var stack = itemStackFromJItem(jitem);
            item.accept(stack);
            return stack;
        });
        registered.put(id, i);
        return i;
    }

    public JItem create(String id) {
        Validate.isTrue(!registered.containsKey(id), "Item with id " + id + " is already registered");
        var i = new JItem(id, this::itemStackFromJItem);
        registered.put(id, i);
        return i;
    }

    private Component getName(String id) {
        /// DO NOT CHANGE THIS
        boolean isGeyser = true;

        /// Because this server is crossplay with bedrock we need to use non-translatable names
        Component component;
        if (isGeyser) {
            component = getGeyserCompatibleName(id);
        } else {
            component = Component.translatable("item.japanese_minecraft." + id);
        }
        return component.decoration(TextDecoration.ITALIC, false);
    }

    private Component getGeyserCompatibleName(String id) {
        var builder = new StringBuilder();

        var strings = id.split("_");

        for (String string : strings) {
            var chars = string.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            builder.append(chars).append(" ");
        }

        return Component.text(builder.toString().trim());
    }

    private ItemStack itemStackFromJItem(JItem item) {
        var id = item.getId();

        var itemStack = new ItemStack(getMaterial(item));
        var meta = itemStack.getItemMeta();
        assert meta != null;

        meta.displayName(getName(id));

        /// custom model data will work on geyser
        /// however the resource pack must be converted to bedrock first
        meta.getCustomModelDataComponent().setFloats(List.of((float)getModel(item)));

        /// stores the item id in the itemstacks persistent data container
        setItemId(meta, item);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private int getModel(JItem item) {
        var id = item.getId();
        Map<String, Object> data = itemsMap.get(id);
        if (data != null && data.get("model") instanceof Number num) {
            return num.intValue();
        }
        return 0;
    }

    private Material getMaterial(JItem item) {
        var id = item.getId();
        Map<String, Object> data = itemsMap.get(id);
        if (data != null && data.get("material") instanceof String material) {
            return Material.getMaterial(material);
        }
        return Material.STONE;
    }

    private void setItemId(ItemMeta meta, JItem item) {
        var id = item.getId();
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, id);
    }

    /// This shouldnt be static. This is a placeholder for now
    public static String getItemId(ItemStack item) {
        var meta = item.getItemMeta();
        if (meta != null) {
            return meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
        }
        return null;
    }
}
