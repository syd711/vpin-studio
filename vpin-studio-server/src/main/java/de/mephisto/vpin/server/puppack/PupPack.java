package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.ScreenMode;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PupPack {
  private final static Logger LOG = LoggerFactory.getLogger(PupPack.class);

  public static final String SCREENS_PUP = "screens.pup";
  public static final String TRIGGERS_PUP = "triggers.pup";
  public static final String PLAYLISTS_PUP = "playlists.pup";

  private ScreensPub screensPup;
  private TriggersPup triggersPup;

  private final File playlistPup;


  private final File packFolder;
  private List<String> options = new ArrayList<>();
  private List<String> txtFiles = new ArrayList<>();
  private String selectedOption = null;
  private List<String> missingResources = new ArrayList<>();

  private long size;

  public PupPack(@NonNull File packFolder) {
    playlistPup = new File(packFolder, PLAYLISTS_PUP);
    this.packFolder = packFolder;
  }

  public List<String> getTxtFiles() {
    return txtFiles;
  }

  public void setTxtFiles(List<String> txtFiles) {
    this.txtFiles = txtFiles;
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

  public String getName() {
    return this.packFolder.getName();
  }

  public File getPupPackFolder() {
    return this.packFolder;
  }

  public ScreenMode getScreenMode(PopperScreen dmd) {
    return this.screensPup.getScreenMode(dmd);
  }

  public void load() {
    try {
      screensPup = new ScreensPub(new File(packFolder, SCREENS_PUP));
      triggersPup = new TriggersPup(new File(packFolder, TRIGGERS_PUP));

      setSize(org.apache.commons.io.FileUtils.sizeOfDirectory(packFolder));

      Collection<File> txtFiles = org.apache.commons.io.FileUtils.listFiles(packFolder, new String[]{"txt"}, true);
      for (File txtFile : txtFiles) {
        if(txtFile.length() > 0) {
          String path = txtFile.getAbsolutePath().replaceAll("\\\\", "/");
          path = path.substring(path.indexOf(packFolder.getName()) + packFolder.getName().length() +1);
          getTxtFiles().add(path);
        }
      }

      this.getOptions().clear();
      this.getMissingResources().clear();
      this.selectedOption = null;

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

              if (screensPup.exists() && triggersPup.exists() && triggersPup.canRead() && playlistsPup.exists()
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

      if (getTriggersPup().exists()) {
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
    } catch (Exception e) {
      LOG.error("Failed to load PUP pack \"" + packFolder.getAbsolutePath() + "\": " + e.getMessage());
    }
  }

  @NotNull
  public JobExecutionResult executeOption(String option) {
    File file = getOptionFile(option);
    if (file.exists()) {
      try {
        LOG.info("Executing PUP pack option \"" + option + "\" for \"" + getPupPackFolder().getName() + "\"");

        String batFile = org.apache.commons.io.FileUtils.readFileToString(file, Charset.defaultCharset());
        boolean tmpCreated = false;
        if (batFile.contains("@pause")) {
          batFile = batFile.replaceAll("@pause", "");
          file = File.createTempFile(FilenameUtils.getBaseName(file.getName()), ".bat", getPupPackFolder());
          file.deleteOnExit();
          org.apache.commons.io.FileUtils.writeStringToFile(file, batFile, Charset.defaultCharset());
          tmpCreated = true;
        }

        SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("\"" + file.getName() + "\""));
        executor.setDir(getPupPackFolder());
        executor.executeCommand();

        if (tmpCreated) {
          file.delete();
        }

        String out = executor.getStandardOutputFromCommand().toString();
        String err = executor.getStandardErrorFromCommand().toString();
        if (!StringUtils.isEmpty(err)) {
          LOG.error("Error executing PUP option " + file.getAbsolutePath() + ": " + err);
          return JobExecutionResultFactory.error(err, out);
        }
        return JobExecutionResultFactory.ok(out, -1);
      } catch (Exception e) {
        LOG.error("Error executing shutdown: " + e.getMessage(), e);
        return JobExecutionResultFactory.error("Error executing shutdown: " + e.getMessage());
      } finally {
        this.load();
      }
    }
    return JobExecutionResultFactory.error("Option command file " + file.getAbsolutePath() + " not found.");
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
