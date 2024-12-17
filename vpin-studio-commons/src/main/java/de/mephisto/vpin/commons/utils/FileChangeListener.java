package de.mephisto.vpin.commons.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.File;

public interface FileChangeListener {

  void notifyFileChange(@Nullable File file);
}
