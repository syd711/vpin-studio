package de.mephisto.vpin.restclient.backups;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BackupFileInfoFactory {

  public static BackupFileInfo create(@NonNull File file) {
    return create(file, null);
  }

  public static BackupFileInfo create(@NonNull File file, @Nullable List<File> files) {
    return create(null, file, files);
  }

  public static BackupFileInfo create(@Nullable String fileName, @Nullable File file, @Nullable List<File> files) {
    if (file != null && file.isDirectory() && files == null) {
      files = new ArrayList<>(FileUtils.listFiles(file, null, true));
    }

    BackupFileInfo info = new BackupFileInfo();
    if (files != null) {
      long size = 0;
      for (File f : files) {
        if (f.exists()) {
          size += f.length();
        }
      }
      info.setFiles(files.size());
      info.setSize(size);
    }
    else {
      if (file != null && file.exists() && file.isFile()) {
        info.setSize(file.length());
      }
    }

    if (file != null) {
      info.setFileName(file.getName());
    }
    else if (fileName != null) {
      info.setFileName(fileName);
    }

    return info;
  }

}
