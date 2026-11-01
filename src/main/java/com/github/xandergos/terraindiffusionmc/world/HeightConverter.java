package com.github.xandergos.terraindiffusionmc.world;

import com.github.xandergos.terraindiffusionmc.pipeline.WorldPipelineModelConfig;

public class HeightConverter {
    private static final int SEA_LEVEL = 63;
    private static final short MAX_PIPELINE_METERS = 10_000;

    private static float getResolutionForScale(int configuredScale) {
        return WorldPipelineModelConfig.nativeResolution() / WorldScaleManager.clampScale(configuredScale);
    }

    public static int convertToMinecraftHeight(short meters) {
        return convertToMinecraftHeight(meters, WorldScaleManager.getCurrentScale());
    }

   public static int convertToMinecraftHeight(short meters, int configuredScale) {
    int baseY;
    float resolution = getResolutionForScale(configuredScale);

    // Tunables — adjust these
    final float MID_START = 150f;   // meters: everything below this gets compressed toward valley height
    final float MID_END   = 800f;   // meters: everything above this is treated as "mountain"
    final float VALLEY_SCALE = 0.25f; // how much of the normal resolution to use for the mid/valley band (lower = flatter)

    if (meters >= MID_END) {
        // Full-height mountains (linear from the MID_END point upward)
        float mountainMeters = meters - MID_END;
        baseY = (int) (mountainMeters / resolution) + (int) ((MID_END - MID_START) * VALLEY_SCALE / resolution) + (int) (MID_START * VALLEY_SCALE / resolution);
    } else if (meters >= 0) {
        // Compress the middle + low positive elevations
        baseY = (int) (meters * VALLEY_SCALE / resolution);
    } else {
        // Existing negative (ocean/trench) compression, slightly tightened
        baseY = (int) (-Math.sqrt(Math.abs(meters) + 10) + Math.sqrt(10.0)) - 1;
        baseY = Math.max(baseY, -25); // hard floor so valleys don't go too deep
    }

    return baseY + SEA_LEVEL;
}

    /**
     * Returns the highest generated block Y expected from pipeline output for a given scale.
     */
    public static int getMaxGeneratedYForScale(int configuredScale) {
        return convertToMinecraftHeight(MAX_PIPELINE_METERS, configuredScale);
    }
}
