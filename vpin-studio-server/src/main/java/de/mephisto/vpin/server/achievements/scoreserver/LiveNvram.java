package de.mephisto.vpin.server.achievements.scoreserver;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The score-server plugin sends the NVRAM region of the PinMAME controller, the .nv file can hold more (or a header).
 * The NVRAM maps describe the .nv file, so a snapshot is copied into the .nv file at the offset where it is found
 * (port of the Watcher's {@code _align_snapshot}).
 */
public class LiveNvram {
  private static final int PROBE = 32;

  private LiveNvram() {
  }

  /**
   * @return the offset of the snapshot inside the .nv file, null if it can't be found
   */
  @Nullable
  public static Integer align(byte @Nullable [] nvFile, byte @Nullable [] snapshot) {
    if (nvFile == null || snapshot == null || nvFile.length == 0 || snapshot.length == 0) {
      return null;
    }
    if (snapshot.length <= nvFile.length && Arrays.equals(nvFile, 0, snapshot.length, snapshot, 0, snapshot.length)) {
      return 0;
    }
    // several 32 byte probes of the snapshot are searched in the file, the most common offset wins
    Map<Integer, Integer> votes = new HashMap<>();
    int step = Math.max(PROBE, snapshot.length / 64);
    for (int pos = 0; pos < Math.max(1, snapshot.length - PROBE); pos += step) {
      int end = Math.min(snapshot.length, pos + PROBE);
      if (distinct(snapshot, pos, end) < 4) {
        continue; // blank areas match anywhere
      }
      int idx = indexOf(nvFile, snapshot, pos, end);
      if (idx >= 0) {
        votes.merge(idx - pos, 1, Integer::sum);
      }
    }
    Map.Entry<Integer, Integer> best = null;
    for (Map.Entry<Integer, Integer> e : votes.entrySet()) {
      if (best == null || e.getValue() > best.getValue()) {
        best = e;
      }
    }
    if (best == null || best.getValue() < 2 || best.getKey() < 0 || best.getKey() + 1 > nvFile.length) {
      return null;
    }
    return best.getKey();
  }

  /**
   * @return a copy of the .nv file with the snapshot at the given offset
   */
  public static byte[] merge(byte[] nvFile, int offset, byte[] snapshot) {
    byte[] result = Arrays.copyOf(nvFile, nvFile.length);
    int len = Math.max(0, Math.min(snapshot.length, nvFile.length - offset));
    System.arraycopy(snapshot, 0, result, offset, len);
    return result;
  }

  private static int distinct(byte[] data, int from, int to) {
    boolean[] seen = new boolean[256];
    int count = 0;
    for (int i = from; i < to; i++) {
      int b = data[i] & 0xFF;
      if (!seen[b]) {
        seen[b] = true;
        count++;
      }
    }
    return count;
  }

  private static int indexOf(byte[] haystack, byte[] needle, int from, int to) {
    int n = to - from;
    outer:
    for (int i = 0; i <= haystack.length - n; i++) {
      for (int j = 0; j < n; j++) {
        if (haystack[i + j] != needle[from + j]) {
          continue outer;
        }
      }
      return i;
    }
    return -1;
  }
}
