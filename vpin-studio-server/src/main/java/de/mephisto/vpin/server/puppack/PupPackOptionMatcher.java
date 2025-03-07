package de.mephisto.vpin.server.puppack;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.mephisto.vpin.server.puppack.PupPack.*;

public class PupPackOptionMatcher {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackOptionMatcher.class);

  public static String getSelectedOption(@NonNull PupPack pupPack) {
    try {
      File packFolder = pupPack.getPupPackFolder();
      String[] optionsBat = packFolder.list((dir, name) -> name.endsWith(".bat"));
      Map<String, File> matches = new HashMap<>();
      if (optionsBat != null) {
        for (String optionBat : optionsBat) {
          String name = FilenameUtils.getBaseName(optionBat);
          pupPack.getOptions().add(name);
        }

        File optionsFolder = new File(packFolder, "PuP-Pack_Options");
        if (optionsFolder.exists()) {
          for (String option : pupPack.getOptions()) {
            File optionFolder = getOptionFolderName(optionsFolder, option);
            if (optionFolder != null) {
              File optionScreens = new File(optionFolder, SCREENS_PUP);
              if (optionScreens.exists() && pupPack.getScreensPup().exists()) {
                long length = optionScreens.length();
                if (length == pupPack.getScreensPup().length()) {
                  matches.put(option, optionScreens);
                }
              }
            }
          }
        }

        if (matches.size() > 1) {
          String selectedScreensPup = getScreensPup(pupPack);

          for (Map.Entry<String, File> stringFileEntry : matches.entrySet()) {
            File optionScreensPup = stringFileEntry.getValue();
            String s = FileUtils.readFileToString(optionScreensPup, StandardCharsets.UTF_8);
            if (s.equals(selectedScreensPup)) {
              return stringFileEntry.getKey();
            }
          }
        }
        else if (matches.size() == 1) {
          return matches.keySet().iterator().next();
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to match selected PUP pack option for {}: {}", pupPack.getName(), e.getMessage(), e);
    }
    return null;
  }

  private static String getScreensPup(@NonNull PupPack pupPack) throws IOException {
    File screensPup = new File(pupPack.getPupPackFolder(), SCREENS_PUP);
    if (screensPup.exists()) {
      return org.apache.commons.io.FileUtils.readFileToString(screensPup, StandardCharsets.UTF_8);
    }
    return "";
  }

  private static File getOptionFolderName(File optionsFolder, String option) {
    File folder = new File(optionsFolder, option);
    if (folder.exists()) {
      return folder;
    }

    File[] subFolders = optionsFolder.listFiles(pathname -> pathname.isDirectory());
    if (subFolders != null) {
      for (File subFolder : subFolders) {
        if (subFolder.getName().startsWith(option) || option.startsWith(subFolder.getName())) {
          return subFolder;
        }
      }
    }

    return null;
  }
}
