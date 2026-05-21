package de.mephisto.vpin.commons.fx.apng;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Holds the result of decoding an APNG file: the fully-composited frames as
 * {@link BufferedImage} objects, their per-frame display durations, and the
 * animation loop count.
 *
 * <p>Produced by {@link ApngUtil#decodeFrames(java.io.InputStream)} and
 * consumed by {@link ApngUtil#encodeApng(ApngDecodeResult)} after the caller
 * has modified the frames as needed.</p>
 */
public class ApngDecodeResult {

    private final List<BufferedImage> frames;
    private final List<Integer>       delaysMs;
    private final int                 numPlays;

    public ApngDecodeResult(List<BufferedImage> frames, List<Integer> delaysMs, int numPlays) {
        this.frames   = frames;
        this.delaysMs = delaysMs;
        this.numPlays = numPlays;
    }

    /** Fully-composited frames in display order, all {@code TYPE_INT_ARGB}. */
    public List<BufferedImage> getFrames() {
        return frames;
    }

    /** Per-frame display duration in milliseconds, same length as {@link #getFrames()}. */
    public List<Integer> getDelaysMs() {
        return delaysMs;
    }

    /** Number of animation loops; {@code 0} means repeat indefinitely. */
    public int getNumPlays() {
        return numPlays;
    }
}