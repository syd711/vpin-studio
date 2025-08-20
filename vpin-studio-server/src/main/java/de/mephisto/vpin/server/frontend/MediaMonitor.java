package de.mephisto.vpin.server.frontend;


import de.mephisto.vpin.commons.utils.FolderChangeListener;
import de.mephisto.vpin.commons.utils.FolderMonitoringThread;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MediaMonitor implements FolderChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(MediaMonitor.class);

  @NonNull
  private final File folder;
  private List<File> files = new ArrayList<>();

  public MediaMonitor(@NonNull File folder) {
    this.folder = folder;
    if (folder.exists()) {
      FolderMonitoringThread monitoringThread = new FolderMonitoringThread(this, true, false);
      monitoringThread.setFolder(folder);
      monitoringThread.startMonitoring();

      notifyFolderChange(folder, null);
    }
  }

  @Override
  public String toString() {
    return folder.getAbsolutePath();
  }

  public List<File> getFiles() {
    return new ArrayList<>(files);
  }

  @Override
  public void notifyFolderChange(@NonNull File f, @Nullable File file) {
//    LOG.info("Notified change for \"" + folder.getAbsolutePath() + "\"");
    File[] list = folder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return new File(dir, name).isFile();
      }
    });

    if (list != null) {
      this.files = new ArrayList<>(Arrays.asList(list));
    }
    else {
      this.files.clear();
    }
  }
}