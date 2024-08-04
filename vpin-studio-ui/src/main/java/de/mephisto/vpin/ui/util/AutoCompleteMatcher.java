package de.mephisto.vpin.ui.util;

import java.util.List;

public interface AutoCompleteMatcher {

  List<String> match(String input);
}
