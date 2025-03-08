package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.frontend.ScreenMode;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PupPack {
  private final static Logger LOG = LoggerFactory.getLogger(PupPack.class);

  public static final String SCREENS_PUP = "screens.pup";
  public static final String TRIGGERS_PUP = "triggers.pup";
  public static final String PLAYLISTS_PUP = "playlists.pup";

  private ScreensPub screensPup;
  private TriggersPup triggersPup;

  private boolean scriptOnly = false;

  private final File packFolder;

  private List<String> options = new ArrayList<>();
  private List<String> txtFiles = new ArrayList<>();
  private String selectedOption = null;
  private List<String> missingResources = new ArrayList<>();

  private long size = 0;

  public PupPack(@NonNull File packFolder) {
    this.packFolder = packFolder;
    this.init();
  }

  public boolean isScriptOnly() {
    return scriptOnly;
  }

  public void setScriptOnly(boolean scriptOnly) {
    this.scriptOnly = scriptOnly;
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

  public ScreenMode getScreenMode(VPinScreen screen) {
    return this.screensPup.getScreenMode(screen);
  }

  public boolean isTransparent(VPinScreen screen) {
    return this.screensPup.isTransparent(screen);
  }

  public void load() {
    if (size == 0) {
      setSize(org.apache.commons.io.FileUtils.sizeOfDirectory(packFolder));
    }
  }

  private void init() {
    try {
      screensPup = new ScreensPub(new File(packFolder, SCREENS_PUP));
      triggersPup = new TriggersPup(new File(packFolder, TRIGGERS_PUP));

      File[] files = packFolder.listFiles(File::isFile);
      if (files != null) {
        List<File> txtFiles = Arrays.stream(files).filter(f -> FilenameUtils.getExtension(f.getName()).equalsIgnoreCase("txt")).collect(Collectors.toList());
        for (File txtFile : txtFiles) {
          if (txtFile.length() > 0) {
            String path = txtFile.getAbsolutePath().replaceAll("\\\\", "/");
            path = path.substring(path.indexOf(packFolder.getName()) + packFolder.getName().length() + 1);
            getTxtFiles().add(path);
          }
        }
      }


      this.getOptions().clear();
      this.getMissingResources().clear();
      this.selectedOption = PupPackOptionMatcher.getSelectedOption(this);


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
    }
    catch (Exception e) {
      LOG.error("Failed to load PUP pack \"" + packFolder.getAbsolutePath() + "\": " + e.getMessage());
    }
  }

  @NonNull
  public JobDescriptor executeOption(String option) {
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
          return JobDescriptorFactory.error(err);
        }
        return JobDescriptorFactory.ok(-1);
      }
      catch (Exception e) {
        LOG.error("Error executing shutdown: " + e.getMessage(), e);
        return JobDescriptorFactory.error("Error executing shutdown: " + e.getMessage());
      }
      finally {
        this.load();
      }
    }
    return JobDescriptorFactory.error("Option command file " + file.getAbsolutePath() + " not found.");
  }

  public String getSelectedOption() {
    return selectedOption;
  }

  public File getOptionFile(String option) {
    return new File(packFolder, option + ".bat");
  }

  public boolean containsFileWithSuffixes(String... suffixes) {
    File[] subFolders = getPupPackFolder().listFiles(File::isDirectory);
    if (subFolders != null) {
      for (File subFolder : subFolders) {
        File[] subFolderFiles = subFolder.listFiles(File::isFile);
        if (subFolderFiles != null) {
          if (containsFileWithSuffixes(subFolderFiles, suffixes)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean containsFileWithSuffixes(File[] subFolderFiles, String[] suffixes) {
    for (File subFolderFile : subFolderFiles) {
      String ending = FilenameUtils.getExtension(subFolderFile.getName());
      for (String suffix : suffixes) {
        if (suffix.equalsIgnoreCase(ending)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "PUP Pack for \"" + packFolder.getName() + "\"";
  }
}
