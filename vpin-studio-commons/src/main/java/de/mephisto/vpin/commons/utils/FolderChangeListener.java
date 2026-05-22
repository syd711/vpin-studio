package de.mephisto.vpin.commons.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;

public interface FolderChangeListener {

  void notifyFolderChange(@NonNull File folder, @Nullable File file);
}
