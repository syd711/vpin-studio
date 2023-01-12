package de.mephisto.vpin.server.vpa;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;

public interface VpaExportListener {

  void exported(@NonNull File file, @NonNull String zipPath);
}
