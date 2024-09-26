package de.mephisto.vpin.commons.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.File;

public interface FolderChangeListener {

  void notifyFolderChange(@NonNull File folder, @Nullable File file);
}
