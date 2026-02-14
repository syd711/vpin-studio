package de.mephisto.vpin.restclient.tagging;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class TaggingUtil {
  public static final List<String> COLORS = Arrays.asList("#FF6B6B", "#AAA93D", "#6BCB77", "#4D96FF", "#9D4EDD", "#00B4D8", "#F81144", "#FFA94D");

  private final static Map<String, String> COLOR_MAP = new HashMap<>();

  public static List<String> getTags(@Nullable String value) {
    List<String> result = new ArrayList<>();
    if (!StringUtils.isEmpty(value)) {
      result.addAll(new ArrayList<>(Arrays.asList(value.split(","))).stream().map(t -> t.trim()).filter(t -> !StringUtils.isEmpty(t)).collect(Collectors.toList()));
    }
    return result;
  }

  public void resetColors() {
    COLOR_MAP.clear();
  }

  public static String getColor(List<String> tags, @NonNull String tag) {
    if (!COLOR_MAP.containsKey(tag)) {
      int index = tags.indexOf(tag) % TaggingUtil.COLORS.size();
      String color = TaggingUtil.COLORS.get(index);
      COLOR_MAP.put(tag, color);
    }
    return COLOR_MAP.get(tag);
  }
}
