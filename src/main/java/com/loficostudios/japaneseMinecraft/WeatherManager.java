package com.loficostudios.japaneseMinecraft;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.block.Biome.*;

public class WeatherManager {
    private static final Map<Biome, Integer> BASE_TEMPERATURE = new HashMap<>();

    private static final int RAIN_TEMP_FACTOR = -3;
    private static final int SNOW_TEMP_FACTOR = -5;
    private static final int THUNDER_TEMP_FACTOR = -1;

    private static final int WATER_TEMP_FACTOR = -2;
    private static final int LAVA_TEMP_FACTOR = 10;

    private static final int POWDERED_SNOW_TEMP_FACTOR = -8;

    private static final int DEFAULT_TEMPERATURE = 20;

    public enum WeatherType { SUNNY, RAINY, STORMY, SNOWY, CLOUDY }

    public WeatherManager() {
    }

    public int getTemperature(Player player) {
        var biome = player.getLocation().getBlock().getBiome();
        int temp = BASE_TEMPERATURE.getOrDefault(biome, WeatherManager.DEFAULT_TEMPERATURE);
        var isSnow = isSnowBiome(biome);
        var world = player.getWorld();

        if (world.hasStorm()) {
            temp += isSnow ? WeatherManager.SNOW_TEMP_FACTOR : WeatherManager.RAIN_TEMP_FACTOR;
        }

        if (world.isThundering()) {
            temp += WeatherManager.THUNDER_TEMP_FACTOR;
        }

        if (player.isInWater()) {
            temp += WeatherManager.WATER_TEMP_FACTOR;
        }

        if (player.isInLava()) {
            temp += WeatherManager.LAVA_TEMP_FACTOR;
        }

        if (player.isInPowderedSnow()) {
            temp += WeatherManager.POWDERED_SNOW_TEMP_FACTOR;
        }


        return temp;
    }

    boolean isSnowBiome(Biome biome) {
        return biome == SNOWY_PLAINS || biome == SNOWY_TAIGA || biome == ICE_SPIKES || biome == SNOWY_BEACH || biome == SNOWY_SLOPES;
    }

    boolean isDesertBiome(Biome biome) {
        return biome == DESERT || biome == SAVANNA || biome == SAVANNA_PLATEAU || biome == BADLANDS || biome == ERODED_BADLANDS || biome == WOODED_BADLANDS;
    }

    public WeatherType getWeatherType(Player player) {
        /// In desert biomes, the weather is cloudy unless there is a thunderstorm, in which case the
        /// storm has a higher priority than cloudy

        var world = player.getWorld();
        var biome = player.getLocation().getBlock().getBiome();
        var storm = world.hasStorm();
        var thunder = world.isThundering();
        var isSnow = isSnowBiome(biome);

        if (storm) {
            if (thunder) {
                return WeatherType.STORMY;
            } else {
                var isDesert = isDesertBiome(biome);
                if (isDesert) {
                    return WeatherType.CLOUDY;
                }
                return isSnow ? WeatherType.SNOWY : WeatherType.RAINY;
            }
        } else {
            return WeatherType.SUNNY;
        }
    }

    static {
        BASE_TEMPERATURE.put(BADLANDS, 35);
        BASE_TEMPERATURE.put(BAMBOO_JUNGLE, 20);
        BASE_TEMPERATURE.put(BASALT_DELTAS, 45);
        BASE_TEMPERATURE.put(BEACH, 25);
        BASE_TEMPERATURE.put(BIRCH_FOREST, 18);
        BASE_TEMPERATURE.put(CHERRY_GROVE, 18);
        BASE_TEMPERATURE.put(COLD_OCEAN, 5);
        BASE_TEMPERATURE.put(CRIMSON_FOREST, 50);
        BASE_TEMPERATURE.put(DARK_FOREST, 17);
        BASE_TEMPERATURE.put(DEEP_COLD_OCEAN, 3);
        BASE_TEMPERATURE.put(DEEP_DARK, 12);
        BASE_TEMPERATURE.put(DEEP_FROZEN_OCEAN, -2);
        BASE_TEMPERATURE.put(DEEP_LUKEWARM_OCEAN, 18);
        BASE_TEMPERATURE.put(DEEP_OCEAN, 12);
        BASE_TEMPERATURE.put(DESERT, 40);
        BASE_TEMPERATURE.put(DRIPSTONE_CAVES, 15);
        BASE_TEMPERATURE.put(END_BARRENS, 15);
        BASE_TEMPERATURE.put(END_HIGHLANDS, 15);
        BASE_TEMPERATURE.put(END_MIDLANDS, 15);
        BASE_TEMPERATURE.put(ERODED_BADLANDS, 33);
        BASE_TEMPERATURE.put(FLOWER_FOREST, 18);
        BASE_TEMPERATURE.put(FOREST, 17);
        BASE_TEMPERATURE.put(FROZEN_OCEAN, -2);
        BASE_TEMPERATURE.put(FROZEN_PEAKS, -5);
        BASE_TEMPERATURE.put(FROZEN_RIVER, -2);
        BASE_TEMPERATURE.put(GROVE, 12);
        BASE_TEMPERATURE.put(ICE_SPIKES, -5);
        BASE_TEMPERATURE.put(JAGGED_PEAKS, 0);
        BASE_TEMPERATURE.put(JUNGLE, 22);
        BASE_TEMPERATURE.put(LUKEWARM_OCEAN, 22);
        BASE_TEMPERATURE.put(LUSH_CAVES, 18);
        BASE_TEMPERATURE.put(MANGROVE_SWAMP, 20);
        BASE_TEMPERATURE.put(MEADOW, 16);
        BASE_TEMPERATURE.put(MUSHROOM_FIELDS, 12);
        BASE_TEMPERATURE.put(NETHER_WASTES, 45);
        BASE_TEMPERATURE.put(OCEAN, 15);
        BASE_TEMPERATURE.put(OLD_GROWTH_BIRCH_FOREST, 16);
        BASE_TEMPERATURE.put(OLD_GROWTH_PINE_TAIGA, 10);
        BASE_TEMPERATURE.put(OLD_GROWTH_SPRUCE_TAIGA, 10);
        BASE_TEMPERATURE.put(PALE_GARDEN, 20);
        BASE_TEMPERATURE.put(PLAINS, 16);
        BASE_TEMPERATURE.put(RIVER, 16);
        BASE_TEMPERATURE.put(SAVANNA, 30);
        BASE_TEMPERATURE.put(SAVANNA_PLATEAU, 32);
        BASE_TEMPERATURE.put(SMALL_END_ISLANDS, 15);
        BASE_TEMPERATURE.put(SNOWY_BEACH, 0);
        BASE_TEMPERATURE.put(SNOWY_PLAINS, -2);
        BASE_TEMPERATURE.put(SNOWY_SLOPES, -3);
        BASE_TEMPERATURE.put(SNOWY_TAIGA, -2);
        BASE_TEMPERATURE.put(SOUL_SAND_VALLEY, 45);
        BASE_TEMPERATURE.put(SPARSE_JUNGLE, 20);
        BASE_TEMPERATURE.put(STONY_PEAKS, 5);
        BASE_TEMPERATURE.put(STONY_SHORE, 12);
        BASE_TEMPERATURE.put(SUNFLOWER_PLAINS, 17);
        BASE_TEMPERATURE.put(SWAMP, 18);
        BASE_TEMPERATURE.put(TAIGA, 10);
        BASE_TEMPERATURE.put(THE_END, 15);
        BASE_TEMPERATURE.put(THE_VOID, 0);
        BASE_TEMPERATURE.put(WARM_OCEAN, 25);
        BASE_TEMPERATURE.put(WARPED_FOREST, 40);
        BASE_TEMPERATURE.put(WINDSWEPT_FOREST, 12);
        BASE_TEMPERATURE.put(WINDSWEPT_GRAVELLY_HILLS, 10);
        BASE_TEMPERATURE.put(WINDSWEPT_HILLS, 12);
        BASE_TEMPERATURE.put(WINDSWEPT_SAVANNA, 28);
        BASE_TEMPERATURE.put(WOODED_BADLANDS, 33);
    }
}
