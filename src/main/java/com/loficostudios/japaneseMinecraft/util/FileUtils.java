package com.loficostudios.japaneseMinecraft.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileUtils {
    /// TBH I don't know what to call this method
    /// This method extracts the folder from the plugin jar to the data folder
    /// I might change this so that the files get loaded into memory instead of being extracted
    /// For now the only implementation for this is in the ItemRegistry class
    public static void extractDataFolderAndUpdate(IPluginResources resources, String folderName, Consumer<File> onFound) {

        /// Delete old folder
        var oldDataFolder = new File(resources.getDataFolder(), folderName);
        if (oldDataFolder.exists()) {
            try (var paths = Files.walk(oldDataFolder.toPath())) {
                paths.sorted(Comparator.reverseOrder())
                        .map(java.nio.file.Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                /// The reason why we are not throwing an error is because the folder isnt there yet
                e.printStackTrace();
            }
        }

        if (!copyFolderFromResources(resources, folderName)) {
            throw new IllegalStateException("Could not copy folder from resources: " + folderName);
        }

        var dataFolder = new File(resources.getDataFolder(), folderName);
        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            throw new IllegalStateException("Could not create or find: " + dataFolder.getAbsolutePath());
        }

        assert dataFolder.listFiles() != null;
        try (var paths = Files.walk(dataFolder.toPath())) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        var file = path.toFile();
                        onFound.accept(file);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean copyFolderFromResources(IPluginResources resources, String folderName) {
        File outDir = new File(resources.getDataFolder(), folderName);
        if (!outDir.exists()) outDir.mkdirs();

        try (JarFile jar = new JarFile(resources.getJarFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith(folderName + "/") && !entry.isDirectory()) {
                    String relativePath = name.substring(folderName.length() + 1);
                    File outFile = new File(outDir, relativePath);
                    outFile.getParentFile().mkdirs();

                    try (InputStream is = jar.getInputStream(entry)) {
                        Files.copy(is, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
