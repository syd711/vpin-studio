package de.mephisto.vpin.restclient.validation;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationState {
  private int code;
  private List<String> options = new ArrayList<>();

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public List<String> getOptions() {
    return options;
  }

  public void setOptions(List<String> options) {
    this.options = options;
  }

  public static String toIdString(List<Integer> ignoredValidations) {
    if(ignoredValidations == null) {
      return "";
    }
    return ignoredValidations.stream().map(String::valueOf).collect(Collectors.joining(","));
  }

  public static List<Integer> toIds(String idsString) {
    List<Integer> ignoredIds = new ArrayList<>();
    if(!StringUtils.isEmpty(idsString)) {
      String[] split = idsString.split(",");
      for (String s : split) {
        try {
          ignoredIds.add(Integer.parseInt(s));
        }
        catch (Exception e) {
          //ignore
        }
      }
    }
    return ignoredIds;
  }

  public static boolean contains(List<ValidationState> validations, int code) {
    for (ValidationState validation : validations) {
      if (validation.code == code) {
        return true;
      }
    }
    return false;
  }
}
