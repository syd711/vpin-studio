package de.mephisto.vpin.tools;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTutorialUrls;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

public class VPSTutorialAnalyzer {

  private final static String VIDEO_PATH = "C:\\workspace\\vpin-studio-assets\\tutorial-videos";

  public static void main(String[] args) {
    VPS vps = new VPS();
    vps.reload();

    File[] files = new File(VIDEO_PATH).listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".mp4");
      }
    });

    List<String> collect = Arrays.stream(files).map(f -> FilenameUtils.getBaseName(f.getName())).collect(Collectors.toList());

    List<VpsTable> tables = vps.getTables();
    for (VpsTable table : tables) {
      List<VpsTutorialUrls> tutorialFiles = table.getTutorialFiles();
      for (VpsTutorialUrls tutorialFile : tutorialFiles) {
        if (tutorialFile.getAuthors().contains("Kongedam") && !collect.contains(table.getId())) {
          System.out.println("Table: " + table.getName() + " / " + table.getId());
        }
      }
    }
  }
}
