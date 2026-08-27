package de.mephisto.vpin.server.fp;

import java.util.Arrays;

/**
 * Decompressor for the LZO1X bitstream, which is what Future Pinball uses for the
 * compressed payloads inside a .fpt file (they are prefixed with the magic "zLZO").
 * <p>
 * Only decompression is implemented, there are no dependencies, and a truncated or
 * misaligned stream returns whatever could be decoded instead of throwing, so a caller
 * can try a few candidate offsets and keep the best result.
 */
public final class Lzo1xDecompressor {

  private static final int STATE_TOP = 0;
  private static final int STATE_FIRST_LITERAL_RUN = 1;
  private static final int STATE_MATCH = 2;
  private static final int STATE_MATCH_DONE = 3;
  private static final int STATE_MATCH_NEXT = 4;

  private Lzo1xDecompressor() {
    //no instances
  }

  /**
   * Decompresses the LZO1X stream that starts at the given offset.
   *
   * @param in     the buffer holding the compressed stream
   * @param offset the first byte of the compressed stream
   * @param maxOut upper bound for the decompressed size, acts as a safety valve
   * @return the decompressed bytes, possibly truncated when the stream was damaged
   */
  public static byte[] decompress(byte[] in, int offset, int maxOut) {
    byte[] out = new byte[Math.min(1 << 16, Math.max(1024, maxOut))];
    int ip = offset;
    int op = 0;
    int t = 0;
    int mPos;
    int state = STATE_TOP;

    try {
      //a first byte above 17 means the stream opens with a literal run
      if ((in[ip] & 0xFF) > 17) {
        t = (in[ip++] & 0xFF) - 17;
        if (t < 4) {
          state = STATE_MATCH_NEXT;
        }
        else {
          out = ensureCapacity(out, op + t, maxOut);
          while (t-- > 0) {
            out[op++] = in[ip++];
          }
          state = STATE_FIRST_LITERAL_RUN;
        }
      }

      while (true) {
        if (state == STATE_TOP) {
          t = in[ip++] & 0xFF;
          if (t >= 16) {
            state = STATE_MATCH;
            continue;
          }
          if (t == 0) {
            while (in[ip] == 0) {
              t += 255;
              ip++;
            }
            t += 15 + (in[ip++] & 0xFF);
          }
          t += 3;
          out = ensureCapacity(out, op + t, maxOut);
          while (t-- > 0) {
            out[op++] = in[ip++];
          }
          state = STATE_FIRST_LITERAL_RUN;
          continue;
        }

        if (state == STATE_FIRST_LITERAL_RUN) {
          t = in[ip++] & 0xFF;
          if (t >= 16) {
            state = STATE_MATCH;
            continue;
          }
          mPos = op - (1 + 0x0800) - (t >> 2) - ((in[ip++] & 0xFF) << 2);
          if (mPos < 0) {
            return Arrays.copyOf(out, op);
          }
          out = ensureCapacity(out, op + 3, maxOut);
          out[op++] = out[mPos++];
          out[op++] = out[mPos++];
          out[op++] = out[mPos];
          state = STATE_MATCH_DONE;
          continue;
        }

        if (state == STATE_MATCH) {
          boolean copied = false;
          if (t >= 64) {
            mPos = op - 1 - ((t >> 2) & 7) - ((in[ip++] & 0xFF) << 3);
            t = (t >> 5) - 1;
          }
          else if (t >= 32) {
            t &= 31;
            if (t == 0) {
              while (in[ip] == 0) {
                t += 255;
                ip++;
              }
              t += 31 + (in[ip++] & 0xFF);
            }
            mPos = op - 1 - (readShort(in, ip) >> 2);
            ip += 2;
          }
          else if (t >= 16) {
            mPos = op - ((t & 8) << 11);
            t &= 7;
            if (t == 0) {
              while (in[ip] == 0) {
                t += 255;
                ip++;
              }
              t += 7 + (in[ip++] & 0xFF);
            }
            mPos -= readShort(in, ip) >> 2;
            ip += 2;
            if (mPos == op) {
              return Arrays.copyOf(out, op); //end of stream marker
            }
            mPos -= 0x4000;
          }
          else {
            mPos = op - 1 - (t >> 2) - ((in[ip++] & 0xFF) << 2);
            if (mPos < 0) {
              return Arrays.copyOf(out, op);
            }
            out = ensureCapacity(out, op + 2, maxOut);
            out[op++] = out[mPos++];
            out[op++] = out[mPos];
            copied = true;
          }

          if (!copied) {
            if (mPos < 0) {
              return Arrays.copyOf(out, op);
            }
            t += 2;
            out = ensureCapacity(out, op + t, maxOut);
            while (t-- > 0) {
              out[op++] = out[mPos++];
            }
          }
          state = STATE_MATCH_DONE;
          continue;
        }

        if (state == STATE_MATCH_DONE) {
          t = in[ip - 2] & 3;
          state = t == 0 ? STATE_TOP : STATE_MATCH_NEXT;
          continue;
        }

        //STATE_MATCH_NEXT: one to three literals, then straight into another match
        out = ensureCapacity(out, op + t, maxOut);
        while (t-- > 0) {
          out[op++] = in[ip++];
        }
        t = in[ip++] & 0xFF;
        state = STATE_MATCH;
      }
    }
    catch (ArrayIndexOutOfBoundsException | NegativeArraySizeException e) {
      //damaged or truncated stream: hand back what was decoded so far
      return Arrays.copyOf(out, Math.max(0, Math.min(op, out.length)));
    }
  }

  private static byte[] ensureCapacity(byte[] out, int needed, int maxOut) {
    if (needed <= out.length) {
      return out;
    }
    if (needed > maxOut) {
      throw new ArrayIndexOutOfBoundsException("output limit of " + maxOut + " bytes reached");
    }
    int size = out.length;
    while (size < needed) {
      size = Math.min(maxOut, size * 2);
    }
    return Arrays.copyOf(out, size);
  }

  private static int readShort(byte[] b, int offset) {
    return (b[offset] & 0xFF) | ((b[offset + 1] & 0xFF) << 8);
  }
}
