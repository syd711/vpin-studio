package de.mephisto.vpin.server.achievements.nvram;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves the ROM whose NVRAM map is used for a ROM name. The tomlogic index only contains exact ROM names,
 * so this is a port of the Achievement Watcher's family fallback ({@code _family_rom_candidates}):
 * e.g. {@code twd_160} falls back to the map of {@code twd_156}.
 */
public class RomMapResolver {

  private static final Pattern TRAILING_LETTERS = Pattern.compile("^([a-z0-9]+?)[a-z]+$");
  private static final Pattern LETTERS_DIGITS = Pattern.compile("^([a-z]+[0-9]+)");
  private static final Pattern LETTERS_US_DIGITS = Pattern.compile("^([a-z]+)_([0-9]+)");

  private final Map<String, String> cache = new ConcurrentHashMap<>();
  private final Set<String> indexRoms;

  public RomMapResolver(@NonNull Collection<String> indexRoms) {
    this.indexRoms = new TreeSet<>(indexRoms);
  }

  /**
   * @return the ROM name to use for the map lookup, or null if there is no map for the ROM or its family.
   */
  @Nullable
  public String resolve(@Nullable String rom) {
    if (rom == null || rom.isBlank()) {
      return null;
    }
    String key = rom.trim().toLowerCase();
    String cached = cache.get(key);
    if (cached != null) {
      return cached.isEmpty() ? null : cached;
    }
    String result = null;
    if (contains(rom.trim())) {
      result = exact(rom.trim());
    }
    else {
      for (String candidate : familyCandidates(rom.trim())) {
        if (contains(candidate)) {
          result = exact(candidate);
          break;
        }
      }
    }
    cache.put(key, result == null ? "" : result);
    return result;
  }

  private boolean contains(String rom) {
    return exact(rom) != null;
  }

  private String exact(String rom) {
    if (indexRoms.contains(rom)) {
      return rom;
    }
    for (String k : indexRoms) {
      if (k.equalsIgnoreCase(rom)) {
        return k;
      }
    }
    return null;
  }

  /**
   * Port of {@code _family_rom_candidates}.
   */
  @NonNull
  public List<String> familyCandidates(@NonNull String rom) {
    String nlow = rom.trim().toLowerCase();
    Set<String> out = new LinkedHashSet<>();

    Matcher m = TRAILING_LETTERS.matcher(nlow);
    if (m.matches()) {
      add(out, m.group(1), nlow);
    }
    String preUs = nlow.split("_")[0];
    if (!preUs.isEmpty() && !preUs.equals(nlow)) {
      add(out, preUs, nlow);
    }
    Matcher m2 = LETTERS_DIGITS.matcher(nlow);
    boolean m2found = m2.find();
    if (m2found) {
      add(out, m2.group(1), nlow);
    }
    if (!preUs.isEmpty() && !preUs.equals(nlow)) {
      Matcher m3 = LETTERS_US_DIGITS.matcher(nlow);
      if (m3.find()) {
        add(out, m3.group(1) + m3.group(2), nlow);
      }
    }
    List<String> familyPrefixes = new ArrayList<>();
    if (m2found) {
      familyPrefixes.add(m2.group(1));
    }
    if (!preUs.isEmpty() && !familyPrefixes.contains(preUs)) {
      familyPrefixes.add(preUs);
    }
    for (String fp : familyPrefixes) {
      for (String key : indexRoms) {
        String kl = key.toLowerCase();
        if (kl.startsWith(fp) && !kl.equals(nlow)) {
          add(out, key, nlow);
        }
      }
    }
    return new ArrayList<>(out);
  }

  private static void add(Set<String> out, String x, String nlow) {
    if (x != null && !x.isBlank() && !x.trim().toLowerCase().equals(nlow)) {
      out.add(x.trim());
    }
  }
}
