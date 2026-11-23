package de.mephisto.vpin.server.achievements;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Title helpers. The key of an achievement is the Achievement Watcher's rarity key: sha1(title.strip())[:12].
 */
public final class AchievementKeys {

  public static final String TITLE_SEPARATOR = " – ";

  private AchievementKeys() {
  }

  @NonNull
  public static String key(@NonNull String title) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] digest = md.digest(title.strip().getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest).substring(0, 12);
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * "afm_113b – Ramps Made: 10" -> "Ramps Made: 10", global titles are returned unchanged.
   */
  @NonNull
  public static String displayTitle(@NonNull String title, @Nullable String rom) {
    if (rom != null && title.startsWith(rom + TITLE_SEPARATOR)) {
      return title.substring(rom.length() + TITLE_SEPARATOR.length());
    }
    int idx = title.indexOf(TITLE_SEPARATOR);
    if (idx > 0 && rom != null && title.substring(0, idx).equalsIgnoreCase(rom)) {
      return title.substring(idx + TITLE_SEPARATOR.length());
    }
    return title;
  }

  /**
   * The ROM prefix of a per ROM title, null for global titles.
   */
  @Nullable
  public static String romOfTitle(@NonNull String title) {
    int idx = title.indexOf(TITLE_SEPARATOR);
    if (idx <= 0) {
      return null;
    }
    String prefix = title.substring(0, idx);
    if (prefix.equals("Global") || prefix.contains(" ")) {
      return null;
    }
    return prefix.toLowerCase();
  }
}
