package de.mephisto.vpin.commons.fx.apng;

import de.mephisto.vpin.commons.fx.apng.chunks.ApngDecoder;
import de.mephisto.vpin.commons.fx.apng.chunks.ApngFrameControl;
import de.mephisto.vpin.commons.fx.apng.image.ApngFrame;
import de.mephisto.vpin.commons.fx.apng.image.ApngFrameDecoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Utility methods for decoding and encoding APNG files.
 *
 * <p>Both methods work at the {@link BufferedImage} level so callers can
 * manipulate frames with standard Java2D without knowing anything about the
 * APNG binary format.</p>
 *
 * <h3>Typical usage</h3>
 * <pre>{@code
 * ApngDecodeResult result = ApngUtil.decodeFrames(inputStream);
 * for (BufferedImage frame : result.getFrames()) {
 *     // modify frame in place …
 * }
 * byte[] apng = ApngUtil.encodeApng(result);
 * }</pre>
 */
public final class ApngUtil {

    // PNG signature
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    // Pre-computed chunk type codes
    private static final int CHUNK_IHDR = chunkTypeCode("IHDR");
    private static final int CHUNK_IDAT = chunkTypeCode("IDAT");
    private static final int CHUNK_IEND = chunkTypeCode("IEND");

    private ApngUtil() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Decodes all frames from an APNG input stream.
     *
     * <p>Every returned frame is a fully-composited, full-canvas
     * {@code TYPE_INT_ARGB} {@link BufferedImage} — DISPOSE_OP and BLEND_OP
     * have already been applied by the underlying {@link ApngFrameDecoder}.
     * The stream is closed by this method.</p>
     *
     * @param input the APNG input stream (will be closed on return)
     * @return decode result containing frames, per-frame delays, and loop count
     * @throws IOException if the stream cannot be read or is not a valid APNG
     */
    public static ApngDecodeResult decodeFrames(InputStream input) throws IOException {
        List<BufferedImage> frames   = new ArrayList<>();
        List<Integer>       delaysMs = new ArrayList<>();
        int                 numPlays;

        try (InputStream in = input) {
            ApngFrameDecoder decoder = new ApngFrameDecoder(in);
            numPlays = decoder.getAnimationNumPlays();
            int colorType = decoder.getColorType();

            ApngFrame apngFrame;
            while ((apngFrame = decoder.nextFrame()) != null) {
                frames.add(toBufferedImage(apngFrame, colorType));
                delaysMs.add(apngFrame.getDelayMillis());
            }
        }

        return new ApngDecodeResult(frames, delaysMs, numPlays);
    }

    /**
     * Encodes an {@link ApngDecodeResult} (possibly with modified frames) back
     * into a valid APNG byte array.
     *
     * @param result the decode result whose frames should be encoded
     * @return raw APNG file bytes ready to be written to disk
     * @throws IOException if any frame cannot be PNG-encoded by {@link ImageIO}
     */
    public static byte[] encodeApng(ApngDecodeResult result) throws IOException {
        return encodeApng(result.getFrames(), result.getDelaysMs(), result.getNumPlays());
    }

    /**
     * Encodes a list of frames into a valid APNG byte array.
     *
     * <h3>Encoding strategy</h3>
     * <p>Each frame is compressed individually by Java's built-in
     * {@link ImageIO} PNG writer; the IDAT payloads are then reassembled with
     * the required APNG envelope chunks ({@code acTL}, {@code fcTL},
     * {@code fdAT}).  No third-party library is required.</p>
     *
     * <p>Frame 0 data is kept as {@code IDAT} (not {@code fdAT}) so that the
     * output file is also a valid static PNG for viewers that do not understand
     * APNG.</p>
     *
     * @param frames    fully-composited frames in display order
     * @param delaysMs  per-frame display duration in milliseconds
     * @param numPlays  number of animation loops ({@code 0} = infinite)
     * @throws IOException if any frame cannot be PNG-encoded by {@link ImageIO}
     */
    public static byte[] encodeApng(
            List<BufferedImage> frames,
            List<Integer>       delaysMs,
            int                 numPlays) throws IOException {

        int numFrames = frames.size();
        int width     = frames.getFirst().getWidth();
        int height    = frames.getFirst().getHeight();

        // Encode frame 0 now so we can borrow its IHDR verbatim.
        byte[] frame0Png = encodeFramePng(frames.getFirst());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream      dos = new DataOutputStream(out);

        // 1. PNG signature
        dos.write(PNG_SIGNATURE);

        // 2. IHDR — copied from the ImageIO-encoded frame 0
        writeChunkFromPng(dos, frame0Png, CHUNK_IHDR);

        // 3. acTL (Animation Control)
        ByteArrayOutputStream actlBuf = new ByteArrayOutputStream(8);
        DataOutputStream      actlDos = new DataOutputStream(actlBuf);
        actlDos.writeInt(numFrames);
        actlDos.writeInt(numPlays);
        writeChunk(dos, "acTL", actlBuf.toByteArray());

        int sequence = 0; // monotonically increasing across fcTL and fdAT chunks

        for (int i = 0; i < numFrames; i++) {

            // 4. fcTL (Frame Control)
            //    Delay: delayMs / 1000 seconds  →  numerator = delayMs, denominator = 1000
            ByteArrayOutputStream fctlBuf = new ByteArrayOutputStream(26);
            DataOutputStream      fctlDos = new DataOutputStream(fctlBuf);
            fctlDos.writeInt(sequence++);                        // sequence_number
            fctlDos.writeInt(width);                             // width
            fctlDos.writeInt(height);                            // height
            fctlDos.writeInt(0);                                 // x_offset
            fctlDos.writeInt(0);                                 // y_offset
            fctlDos.writeShort(delaysMs.get(i));                 // delay_num  (ms)
            fctlDos.writeShort(1000);                            // delay_den  (1/1000 s = 1 ms)
            fctlDos.writeByte(ApngFrameControl.DISPOSE_OP_NONE);
            fctlDos.writeByte(ApngFrameControl.BLEND_OP_SOURCE);
            writeChunk(dos, "fcTL", fctlBuf.toByteArray());

            // 5. Image data
            byte[]       framePng   = (i == 0) ? frame0Png : encodeFramePng(frames.get(i));
            List<byte[]> idatBlocks = extractIdatBlocks(framePng);

            if (i == 0) {
                // Frame 0 → standard IDAT (keeps file valid as a static PNG)
                for (byte[] block : idatBlocks) {
                    writeChunk(dos, "IDAT", block);
                }
            } else {
                // Frames 1+ → fdAT with a leading 4-byte sequence number
                for (byte[] block : idatBlocks) {
                    ByteArrayOutputStream fdatBuf = new ByteArrayOutputStream(4 + block.length);
                    DataOutputStream      fdatDos = new DataOutputStream(fdatBuf);
                    fdatDos.writeInt(sequence++);
                    fdatDos.write(block);
                    writeChunk(dos, "fdAT", fdatBuf.toByteArray());
                }
            }
        }

        // 6. IEND
        writeChunk(dos, "IEND", new byte[0]);

        dos.flush();
        return out.toByteArray();
    }

    // -------------------------------------------------------------------------
    // Frame conversion
    // -------------------------------------------------------------------------

    /**
     * Converts a fully-composited {@link ApngFrame} into a
     * {@code TYPE_INT_ARGB} {@link BufferedImage}.
     *
     * <p>Always produces {@code TYPE_INT_ARGB} regardless of the source color
     * type so that any subsequent Java2D drawing (e.g. compositing a badge)
     * handles alpha correctly even when the original APNG has no alpha
     * channel.</p>
     */
    private static BufferedImage toBufferedImage(ApngFrame frame, int colorType) {
        int    width  = frame.getWidth();
        int    height = frame.getHeight();
        int    bpp    = frame.getBpp();
        byte[] src    = frame.getBytes();

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int base = (y * width + x) * bpp;

                int argb = switch (colorType) {

                    // 4 bytes: R G B A
                    case ApngDecoder.PNG_COLOR_RGB_ALPHA ->
                            ((src[base + 3] & 0xFF) << 24)
                                    | ((src[base    ] & 0xFF) << 16)
                                    | ((src[base + 1] & 0xFF) <<  8)
                                    |  (src[base + 2] & 0xFF);

                    // 3 bytes: R G B  (fully opaque)
                    case ApngDecoder.PNG_COLOR_RGB ->
                            (0xFF << 24)
                                    | ((src[base    ] & 0xFF) << 16)
                                    | ((src[base + 1] & 0xFF) <<  8)
                                    |  (src[base + 2] & 0xFF);

                    // 2 bytes: Y A  (greyscale + alpha)
                    case ApngDecoder.PNG_COLOR_GRAY_ALPHA -> {
                        int luma  = src[base    ] & 0xFF;
                        int alpha = src[base + 1] & 0xFF;
                        yield (alpha << 24) | (luma << 16) | (luma << 8) | luma;
                    }

                    // 1 byte: Y  (greyscale, fully opaque)
                    default -> {
                        int luma = src[base] & 0xFF;
                        yield (0xFF << 24) | (luma << 16) | (luma << 8) | luma;
                    }
                };

                img.setRGB(x, y, argb);
            }
        }
        return img;
    }

    // -------------------------------------------------------------------------
    // PNG chunk helpers
    // -------------------------------------------------------------------------

    /** Encodes a single {@link BufferedImage} as a standard PNG byte array. */
    private static byte[] encodeFramePng(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", baos)) {
            throw new IOException("ImageIO found no suitable PNG writer for the supplied BufferedImage");
        }
        return baos.toByteArray();
    }

    /**
     * Parses a PNG byte array and returns the raw data payload of every
     * {@code IDAT} chunk (length / type / CRC stripped; recalculated on write).
     */
    private static List<byte[]> extractIdatBlocks(byte[] pngBytes) throws IOException {
        List<byte[]>    blocks = new ArrayList<>();
        DataInputStream dis    = new DataInputStream(new ByteArrayInputStream(pngBytes));

        dis.skipBytes(8); // PNG signature

        while (true) {
            int length;
            try {
                length = dis.readInt();
            } catch (EOFException e) {
                break;
            }
            int    type = dis.readInt();
            byte[] data = new byte[length];
            if (length > 0) dis.readFully(data);
            dis.readFully(new byte[4]); // CRC — discarded; recalculated on write

            if (type == CHUNK_IDAT) {
                blocks.add(data);
            } else if (type == CHUNK_IEND) {
                break;
            }
        }
        return blocks;
    }

    /**
     * Finds the first chunk of {@code targetType} in {@code pngBytes} and
     * writes it as a well-formed chunk to {@code dos}.
     */
    private static void writeChunkFromPng(DataOutputStream dos, byte[] pngBytes, int targetType)
            throws IOException {

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pngBytes));
        dis.skipBytes(8); // PNG signature

        while (true) {
            int    length = dis.readInt();
            int    type   = dis.readInt();
            byte[] data   = new byte[length];
            if (length > 0) dis.readFully(data);
            dis.readFully(new byte[4]); // CRC

            if (type == targetType) {
                writeChunk(dos, type, data);
                return;
            }
            if (type == CHUNK_IEND) break;
        }
        throw new IOException(
                "Required PNG chunk not found in encoded frame (type=0x"
                        + Integer.toHexString(targetType) + ")");
    }

    /** Writes a single PNG/APNG chunk: {@code [length][type][data][CRC32]}. */
    private static void writeChunk(DataOutputStream dos, String typeName, byte[] data)
            throws IOException {
        writeChunk(dos, chunkTypeCode(typeName), data);
    }

    private static void writeChunk(DataOutputStream dos, int type, byte[] data)
            throws IOException {

        int dataLen = (data == null) ? 0 : data.length;

        CRC32 crc = new CRC32();
        crc.update((type >>> 24) & 0xFF);
        crc.update((type >>> 16) & 0xFF);
        crc.update((type >>>  8) & 0xFF);
        crc.update( type         & 0xFF);
        if (dataLen > 0) crc.update(data, 0, dataLen);

        dos.writeInt(dataLen);
        dos.writeInt(type);
        if (dataLen > 0) dos.write(data, 0, dataLen);
        dos.writeInt((int) crc.getValue());
    }

    /**
     * Converts a 4-character ASCII chunk name to its 4-byte integer type code.
     * All standard PNG/APNG chunk names use ASCII letters ({@code < 128}),
     * so no sign-extension occurs during the shifts.
     */
    private static int chunkTypeCode(String name) {
        byte[] b = name.getBytes();
        return (b[0] << 24) | (b[1] << 16) | (b[2] << 8) | b[3];
    }
}