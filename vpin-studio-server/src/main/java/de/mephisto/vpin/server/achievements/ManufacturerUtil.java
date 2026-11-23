package de.mephisto.vpin.server.achievements;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes manufacturer names and contains the Achievement Watcher's ROM prefix table, which is used as
 * fallback for games without a VPS match (the VPS manufacturer is preferred).
 */
public final class ManufacturerUtil {

  private static final Pattern LEADING_ALPHA = Pattern.compile("^([a-z]+)(?=[0-9]|_|$)");
  private static final Map<String, String> ROM_PREFIXES = new HashMap<>();

  static {
    Map<String, String> m = ROM_PREFIXES;
    m.put("afm", "Bally");
    m.put("tom", "Bally");
    m.put("mm", "Bally");
    m.put("cv", "Bally");
    m.put("cp", "Bally");
    m.put("cftbl", "Bally");
    m.put("pz", "Bally");
    m.put("fh", "Bally");
    m.put("bbb", "Bally");
    m.put("trucksp", "Bally");
    m.put("theatre", "Bally");
    m.put("scared", "Bally");
    m.put("eatpm", "Bally");
    m.put("centaur", "Bally");
    m.put("paragon", "Bally");
    m.put("eightball", "Bally");
    m.put("medusa", "Bally");
    m.put("xenon", "Bally");
    m.put("vector", "Bally");
    m.put("embryon", "Bally");
    m.put("speakesy", "Bally");
    m.put("hotdoggin", "Bally");
    m.put("mystic", "Bally");
    m.put("fireball", "Bally");
    m.put("frontier", "Bally");
    m.put("harlem", "Bally");
    m.put("ngndshkr", "Bally");
    m.put("goldball", "Bally");
    m.put("grandslm", "Bally");
    m.put("kosteel", "Bally");
    m.put("xsandos", "Bally");
    m.put("blackblt", "Bally");
    m.put("cybrnaut", "Bally");
    m.put("beatclck", "Bally");
    m.put("atlantis", "Bally");
    m.put("spy_hunter", "Bally");
    m.put("flashgdn", "Bally");
    m.put("smman", "Bally");
    m.put("ts", "Williams");
    m.put("t2", "Williams");
    m.put("ij", "Williams");
    m.put("wcs", "Williams");
    m.put("dw", "Williams");
    m.put("br", "Williams");
    m.put("rs", "Williams");
    m.put("ft", "Williams");
    m.put("gi", "Williams");
    m.put("hurr", "Williams");
    m.put("dm", "Williams");
    m.put("tz", "Williams");
    m.put("ww", "Williams");
    m.put("taf", "Williams");
    m.put("nf", "Williams");
    m.put("bop", "Williams");
    m.put("whirl", "Williams");
    m.put("rollr", "Williams");
    m.put("ss", "Williams");
    m.put("taxi", "Williams");
    m.put("pool", "Williams");
    m.put("diner", "Williams");
    m.put("jy", "Williams");
    m.put("poto", "Williams");
    m.put("esha", "Williams");
    m.put("fire", "Williams");
    m.put("sttng", "Williams");
    m.put("jd", "Williams");
    m.put("afv", "Williams");
    m.put("cc", "Williams");
    m.put("corv", "Williams");
    m.put("dh", "Williams");
    m.put("i500", "Williams");
    m.put("jb", "Williams");
    m.put("jm", "Williams");
    m.put("ngg", "Williams");
    m.put("pop", "Williams");
    m.put("sc", "Williams");
    m.put("sf2", "Williams");
    m.put("tod", "Williams");
    m.put("totan", "Williams");
    m.put("wd", "Williams");
    m.put("congo", "Williams");
    m.put("dracula", "Williams");
    m.put("mb", "Williams");
    m.put("nbaf", "Williams");
    m.put("cactjack", "Williams");
    m.put("strik", "Williams");
    m.put("godzilla", "Stern");
    m.put("deadpool", "Stern");
    m.put("got", "Stern");
    m.put("munsters", "Stern");
    m.put("aerosmith", "Stern");
    m.put("lotr", "Stern");
    m.put("sopranos", "Stern");
    m.put("simpsons", "Stern");
    m.put("metallica", "Stern");
    m.put("twd", "Stern");
    m.put("mustang", "Stern");
    m.put("starwars", "Stern");
    m.put("ghostbusters", "Stern");
    m.put("batman66", "Stern");
    m.put("kiss", "Stern");
    m.put("wpt", "Stern");
    m.put("elvis", "Stern");
    m.put("ironman", "Stern");
    m.put("xmen", "Stern");
    m.put("transformers", "Stern");
    m.put("avatar", "Stern");
    m.put("tron", "Stern");
    m.put("acdc", "Stern");
    m.put("spider", "Stern");
    m.put("avengers", "Stern");
    m.put("nbafastbreak", "Stern");
    m.put("lw3", "Data East");
    m.put("tftc", "Data East");
    m.put("hook", "Data East");
    m.put("btmn", "Data East");
    m.put("rab", "Data East");
    m.put("gnr", "Data East");
    m.put("stwr", "Data East");
    m.put("tmnt", "Data East");
    m.put("trek", "Data East");
    m.put("simp", "Data East");
    m.put("wwfr", "Data East");
    m.put("mn_180", "Data East");
    m.put("rctycn", "Data East");
    m.put("aar", "Data East");
    m.put("cue", "Gottlieb");
    m.put("teed", "Gottlieb");
    m.put("sprbrk", "Gottlieb");
    m.put("gladiatr", "Gottlieb");
    m.put("shaq", "Gottlieb");
    m.put("freddy", "Gottlieb");
    m.put("wipe", "Gottlieb");
    m.put("sfight2", "Gottlieb");
    m.put("silvslug", "Gottlieb");
    m.put("waterwld", "Gottlieb");
    m.put("baywatch", "Sega");
    m.put("mav", "Sega");
    m.put("frankenstein", "Sega");
    m.put("id4", "Sega");
    m.put("twister", "Sega");
    m.put("apollo", "Sega");
    m.put("gw", "Sega");
    m.put("jpark", "Sega");
    m.put("swtril", "Sega");
    m.put("spacejam", "Sega");
    m.put("viprsega", "Sega");
    m.put("ctcheese", "Sega");
    m.put("goldeneye", "Sega");
    m.put("xfiles", "Sega");
    m.put("starship", "Sega");
    m.put("harley", "Sega");
    m.put("godzilla_sega", "Sega");
    m.put("lostspc", "Sega");
    m.put("kp", "Capcom");
    m.put("bbb_capcom", "Capcom");
    m.put("pm", "Capcom");
    m.put("flip", "Capcom");
    m.put("bsv", "Capcom");
    m.put("kingspin", "Capcom");  }

  private ManufacturerUtil() {
  }

  /**
   * Port of {@code _get_manufacturer_from_rom} (without the romnames fallback).
   */
  @Nullable
  public static String fromRom(@Nullable String rom) {
    if (rom == null || rom.isBlank()) {
      return null;
    }
    String r = rom.toLowerCase();
    if (ROM_PREFIXES.containsKey(r)) {
      return ROM_PREFIXES.get(r);
    }
    String[] parts = r.split("_");
    for (int i = parts.length - 1; i > 0; i--) {
      String prefix = String.join("_", java.util.Arrays.copyOfRange(parts, 0, i));
      if (ROM_PREFIXES.containsKey(prefix)) {
        return ROM_PREFIXES.get(prefix);
      }
    }
    Matcher matcher = LEADING_ALPHA.matcher(r);
    if (matcher.find() && ROM_PREFIXES.containsKey(matcher.group(1))) {
      return ROM_PREFIXES.get(matcher.group(1));
    }
    return null;
  }

  /**
   * Maps VPS manufacturer names to the names used by the achievement rules
   * (Bally, Williams, Stern, Data East, Gottlieb, Sega, Capcom, Premier, Midway).
   */
  @Nullable
  public static String normalize(@Nullable String manufacturer) {
    if (manufacturer == null || manufacturer.isBlank()) {
      return null;
    }
    String m = manufacturer.trim();
    String l = m.toLowerCase();
    if (l.startsWith("bally")) return "Bally";
    if (l.startsWith("williams")) return "Williams";
    if (l.startsWith("stern")) return "Stern";
    if (l.startsWith("data east")) return "Data East";
    if (l.startsWith("gottlieb")) return "Gottlieb";
    if (l.startsWith("premier")) return "Premier";
    if (l.startsWith("sega")) return "Sega";
    if (l.startsWith("capcom")) return "Capcom";
    if (l.startsWith("midway")) return "Midway";
    return m;
  }
}
