package de.mephisto.vpin.server.achievements.nvram;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * Decoded audits of a .nv file (label -> Long or String) plus the player count of the last game if the map knows it.
 */
public record NvramSnapshot(@NonNull Map<String, Object> audits, @Nullable Integer playerCount) {

  public static NvramSnapshot empty() {
    return new NvramSnapshot(Collections.emptyMap(), null);
  }

  public boolean isEmpty() {
    return audits.isEmpty();
  }
}
