package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.ScreenMode;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PupPack {
  public static final String SCREENS_PUP = "screens.pup";
  public static final String TRIGGERS_PUP = "triggers.pup";

  private final ScreensPub screensPup;
  private final TriggersPup triggersPup;

  private final File packFolder;
  private List<String> options = new ArrayList<>();

  private long size;

  public PupPack(@NonNull File packFolder) {
    screensPup = new ScreensPub(new File(packFolder, SCREENS_PUP));
    triggersPup = new TriggersPup(new File(packFolder, TRIGGERS_PUP));

    this.packFolder = packFolder;
  }

  public List<String> getOptions() {
    return options;
  }

  public void setOptions(List<String> options) {
    this.options = options;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public long getSize() {
    return size;
  }

  public ScreensPub getScreensPup() {
    return screensPup;
  }

  public TriggersPup getTriggersPup() {
    return triggersPup;
  }

  public boolean delete() {
    if (packFolder.exists()) {
      return FileUtils.deleteFolder(packFolder);
    }
    return true;
  }

  public File getPupPackFolder() {
    return this.packFolder;
  }

  public ScreenMode getScreenMode(PopperScreen dmd) {
    return this.screensPup.getScreenMode(dmd);
  }

  public void load() {
    setSize(org.apache.commons.io.FileUtils.sizeOfDirectory(packFolder));

    String[] list = packFolder.list((dir, name) -> name.endsWith(".bat"));
    if (list != null) {
      for (String s : list) {
        if (s.toLowerCase().contains("option")) {
          String name = FilenameUtils.getBaseName(s);
          getOptions().add(name);
        }
      }
    }
  }

  public File getOptionFile(String option) {
    return new File(packFolder, option + ".bat");
  }

  @Override
  public String toString() {
    return "PUP Pack for \"" + packFolder.getName() + "\"";
  }
}
