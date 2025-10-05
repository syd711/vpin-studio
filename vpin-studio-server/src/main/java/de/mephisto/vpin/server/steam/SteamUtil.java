package de.mephisto.vpin.server.steam;

import de.mephisto.vpin.commons.utils.WinRegistry;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SteamUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SteamService.class);

  @Nullable
  public static File getSteamFolder() {
    try {
      Map<String, Object> currentUserValues = WinRegistry.getCurrentUserValues("Software\\Valve\\Steam");
      String steamPath = (String) currentUserValues.get("SteamPath");
      if (!StringUtils.isEmpty(steamPath)) {
        File f = new File(steamPath);
        if (f.exists()) {
          return f;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to resolve Steam: {}", e.getMessage(), e);
    }
    return null;
  }

  @NonNull
  public static Map<String, File> getGameFolders() {
    Map<String, File> gameFolders = new HashMap<>();
    try {
      File steamFolder = getSteamFolder();
      if (steamFolder != null) {
        File libConfigFile = new File(steamFolder, "config/libraryfolders.vdf");
        if (libConfigFile.exists()) {
          List<String> lines = FileUtils.readLines(libConfigFile, StandardCharsets.UTF_8);
          for (String line : lines) {
            if (line.contains("path")) {
              String[] split = line.split("\\t");
              String path = split[split.length - 1].replaceAll("\"", "");
              File appsFolder = new File(path, "steamapps/common");
              if (appsFolder.exists()) {
                File[] files = appsFolder.listFiles(new FileFilter() {
                  @Override
                  public boolean accept(File pathname) {
                    return pathname.isDirectory();
                  }
                });

                for (File file : files) {
                  gameFolders.put(file.getName(), file);
                }
              }
            }
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to initialize SteamService: {}", e.getMessage(), e);
    }

    return gameFolders;
  }

  public static void main(String[] args) {
    List<ProcessHandle> processHandles = ProcessHandle.allProcesses().collect(Collectors.toList());
    for (ProcessHandle processHandle : processHandles) {
      if(processHandle.info() != null && processHandle.info().command().isPresent()) {
        System.out.println(processHandle.info().command().get());
      }
    }
    System.out.println(getGameFolders());
  }
}
