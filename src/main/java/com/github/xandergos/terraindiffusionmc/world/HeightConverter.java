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
    
        // Tunables
        final float VALLEY_END   = 120f;   // meters: everything below this stays low / thin
        final float MID_END      = 2500f;  // meters: end of the compressed mid-range
        final float HIGH_BOOST   = 1.45f;  // multiplier for mountains above MID_END
        final float VALLEY_SCALE = 0.18f;  // how flat/low the valley band stays
        final float MID_SCALE    = 0.35f;  // how much of the 1k–3k band survives (lower = fewer mid mountains)
    
        if (meters >= MID_END) {
            // Boost the high mountains (4k–6k+ range)
            float highMeters = meters - MID_END;
            float midBand = (MID_END - VALLEY_END) * MID_SCALE;
            float valleyBand = VALLEY_END * VALLEY_SCALE;
            baseY = (int) ((highMeters * HIGH_BOOST / resolution) + (midBand / resolution) + (valleyBand / resolution));
        } else if (meters >= VALLEY_END) {
            // Compress the mid-range (roughly 1k–3k) so fewer mountains sit there
            float midMeters = meters - VALLEY_END;
            baseY = (int) (midMeters * MID_SCALE / resolution) + (int) (VALLEY_END * VALLEY_SCALE / resolution);
        } else if (meters >= 0) {
            // Thin valleys, kept low and close to sea level
            baseY = (int) (meters * VALLEY_SCALE / resolution);
        } else {
            // Oceans / trenches stay compressed
            baseY = (int) (-Math.sqrt(Math.abs(meters) + 10) + Math.sqrt(10.0)) - 1;
            baseY = Math.max(baseY, -20);
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
