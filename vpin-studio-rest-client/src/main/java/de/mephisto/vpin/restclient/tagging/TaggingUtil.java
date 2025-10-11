package de.mephisto.vpin.restclient.tagging;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class TaggingUtil {

  public static List<String> getTags(@Nullable String value) {
    List<String> result = new ArrayList<>();
    if (!StringUtils.isEmpty(value)) {
      result.addAll(new ArrayList<>(Arrays.asList(value.split(","))).stream().map(t -> t.trim()).filter(t -> !StringUtils.isEmpty(t)).collect(Collectors.toList()));
    }
    return result;
  }
}
