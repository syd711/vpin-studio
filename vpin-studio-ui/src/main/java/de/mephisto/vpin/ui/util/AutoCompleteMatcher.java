package de.mephisto.vpin.ui.util;

import java.util.List;

public interface AutoCompleteMatcher {

  List<AutoMatchModel> match(String input);

  default void setEntries(List<String> entries) {

  }
}
