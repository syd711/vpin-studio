package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.representations.ValidationState;

import java.util.Arrays;
import java.util.List;

public class GameValidationStateFactory {

  public static ValidationState create(int code) {
    ValidationState state = new ValidationState();
    state.setCode(code);
    return state;
  }

  public static ValidationState create(int code, String... options) {
    ValidationState state = new ValidationState();
    state.setCode(code);
    state.setOptions(Arrays.asList(options));
    return state;
  }

  public static ValidationState create(int code, List<String> options) {
    ValidationState state = new ValidationState();
    state.setCode(code);
    state.setOptions(options);
    return state;
  }

  public static ValidationState empty() {
    return new ValidationState();
  }
}
