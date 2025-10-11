package de.mephisto.vpin.restclient.tagging;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TaggingUtil {
  public static final List<String> COLORS = Arrays.asList("#FF6B6B", "#AAA93D", "#6BCB77", "#4D96FF", "#9D4EDD", "#00B4D8", "#F81144", "#FFA94D");

  public static List<String> getTags(@Nullable String value) {
    List<String> result = new ArrayList<>();
    if (!StringUtils.isEmpty(value)) {
      result.addAll(new ArrayList<>(Arrays.asList(value.split(","))).stream().map(t -> t.trim()).filter(t -> !StringUtils.isEmpty(t)).collect(Collectors.toList()));
    }
    return result;
  }
}
