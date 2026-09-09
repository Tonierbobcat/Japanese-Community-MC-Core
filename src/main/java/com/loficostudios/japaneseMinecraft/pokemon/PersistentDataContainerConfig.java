package com.loficostudios.japaneseMinecraft.pokemon;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import javax.print.attribute.standard.MediaSize;
import java.io.*;
import java.util.function.Consumer;

public class PersistentDataContainerConfig {
    private final String NAMESPACE = "config";
    private final PersistentDataContainer container;
    private final Consumer<PersistentDataContainer> update;

    public PersistentDataContainerConfig(PersistentDataContainer container, Consumer<PersistentDataContainer> update) {
        this.container = container;
        this.update = update;
    }

    public <T> @Nullable T get(String path, Class<T> clazz) {
        var bytes = container.get(new NamespacedKey(NAMESPACE, path), PersistentDataType.BYTE_ARRAY);
        if (bytes == null)
            return null;
        try {
            return deserialize(bytes, clazz);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> void set(String path, T t) {
        try {
            container.set(new NamespacedKey(NAMESPACE, path), PersistentDataType.BYTE_ARRAY, serialize(t));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (update == null)
            return;
        update.accept(container);
    }

    private static <T> byte[] serialize(T object) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(object);
        }

        return bytes.toByteArray();
    }

    private static <T> T deserialize(byte[] data, Class<T> clazz)
            throws IOException, ClassNotFoundException {

        try (ObjectInputStream in =
                     new ObjectInputStream(new ByteArrayInputStream(data))) {

            Object object = in.readObject();

            if (!clazz.isInstance(object)) {
                throw new IllegalArgumentException(
                        "Expected " + clazz.getName() +
                                " but got " + object.getClass().getName()
                );
            }

            return clazz.cast(object);
        }
    }
}
