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

    // Tunables — valleys stay low, middle band is crushed
    final float MID_START = 80f;      // meters: start of the compressed band
    final float MID_END   = 1400f;    // meters: end of the compressed band
    final float MID_SCALE = 0.08f;    // how much of normal height the middle band keeps (lower = flatter)

    if (meters >= MID_END) {
        // Full-height mountains above MID_END
        float mountainMeters = meters - MID_END;
        float midBandHeight = (MID_END - MID_START) * MID_SCALE;
        baseY = (int) ((mountainMeters / resolution) + (midBandHeight / resolution));
    } else if (meters >= MID_START) {
        // Crush the middle elevation band
        float midMeters = meters - MID_START;
        baseY = (int) (midMeters * MID_SCALE / resolution);
    } else if (meters >= 0) {
        // Low positive elevations (true valleys) stay near normal height
        baseY = (int) (meters / resolution);
    } else {
        // Existing negative compression, with a floor so they don't go too deep
        baseY = (int) (-Math.sqrt(Math.abs(meters) + 10) + Math.sqrt(10.0)) - 1;
        baseY = Math.max(baseY, -25);
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
