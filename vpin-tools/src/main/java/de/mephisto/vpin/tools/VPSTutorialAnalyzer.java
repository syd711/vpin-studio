package de.mephisto.vpin.tools;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTutorialUrls;
import de.mephisto.vpin.restclient.util.HttpUtils;
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
    List<VpsTable> tables = vps.getTables();
    for (VpsTable table : tables) {
      List<VpsTutorialUrls> tutorialFiles = table.getTutorialFiles();
      for (VpsTutorialUrls tutorialFile : tutorialFiles) {
        if (tutorialFile.getAuthors().contains("Kongedam")) {
          String videoUrl = "https://assets.vpin-mania.net/tutorials/kongedam/" + table.getId() + ".mp4";
          boolean check = HttpUtils.check(videoUrl);
          if (!check) {
            System.out.println("failed to load " + table.getName());
          }
          else {
            System.out.println(table.getName() + " [OK]");
          }
        }
      }
    }
  }
}
