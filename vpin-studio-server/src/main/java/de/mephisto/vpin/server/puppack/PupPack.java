package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.ScreenMode;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PupPack {
  public static final String SCREENS_PUP = "screens.pup";
  public static final String TRIGGERS_PUP = "triggers.pup";
  public static final String PLAYLISTS_PUP = "playlists.pup";

  private final ScreensPub screensPup;
  private final TriggersPup triggersPup;
  private final File playlistPup;


  private final File packFolder;
  private List<String> options = new ArrayList<>();
  private String selectedOption = null;
  private List<String> missingResources = new ArrayList<>();

  private long size;

  public PupPack(@NonNull File packFolder) {
    screensPup = new ScreensPub(new File(packFolder, SCREENS_PUP));
    triggersPup = new TriggersPup(new File(packFolder, TRIGGERS_PUP));
    playlistPup = new File(packFolder, PLAYLISTS_PUP);

    this.packFolder = packFolder;
  }

  public List<String> getMissingResources() {
    return missingResources;
  }

  public void setMissingResources(List<String> missingResources) {
    this.missingResources = missingResources;
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

    String[] optionsBat = packFolder.list((dir, name) -> name.endsWith(".bat"));
    if (optionsBat != null) {
      for (String optionBat : optionsBat) {
        if (optionBat.toLowerCase().contains("option")) {
          String name = FilenameUtils.getBaseName(optionBat);
          getOptions().add(name);
        }
      }

      File optionsFolder = new File(packFolder, "PuP-Pack_Options");
      if (optionsFolder.exists()) {
        for (String option : getOptions()) {
          File optionFolder = new File(optionsFolder, option);

          if (optionFolder.exists()) {
            File screensPup = new File(optionFolder, SCREENS_PUP);
            File triggersPup = new File(optionFolder, TRIGGERS_PUP);
            File playlistsPup = new File(optionFolder, PLAYLISTS_PUP);

            if (screensPup.exists() && triggersPup.exists() && playlistsPup.exists()
                && this.screensPup.exists() && this.triggersPup.exists() && this.playlistPup.exists()) {
              long length = screensPup.length() + triggersPup.length() + playlistsPup.length();
              if (length == this.playlistPup.length() + this.screensPup.length() + this.triggersPup.length()) {
                selectedOption = option;
                break;
              }
            }
          }
        }
      }
    }

    List<TriggerEntry> entries = getTriggersPup().getEntries();
    for (TriggerEntry entry : entries) {
      if (entry.isActive()) {
        String path = entry.getPlayList();
        String file = entry.getPlayFile();
        if (!StringUtils.isEmpty(file)) {
          path = path + "/" + file;
        }
        File resource = new File(packFolder, path);
        if (!resource.exists()) {
          missingResources.add(path);
        }
      }
    }
  }

  public String getSelectedOption() {
    return selectedOption;
  }

  public File getOptionFile(String option) {
    return new File(packFolder, option + ".bat");
  }

  @Override
  public String toString() {
    return "PUP Pack for \"" + packFolder.getName() + "\"";
  }
}
