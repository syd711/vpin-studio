package de.mephisto.vpin.tools;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NVRamSynchronizer {

  public final static File NVRAM_REPO = new File("../nvrams/");

  private final static String README = "# nvrams\n" +
      "A repository with resetted nvram files for Visual Pinball.\n" +
      "\n" +
      "List of available and missing nvrams:\n" +
      "\n" +
      "| Table | ROM | Download | Submitted By |\n" +
      "| ----- | --- | --------- |--------------|\n";

  public static void main(String[] args) throws Exception {
    synchonizeNVRamRepo();
  }

  private static void synchonizeNVRamRepo() throws Exception {
    File init = new File("./resources/pinemhi/pinemhi.ini");
    File readme = new File(NVRAM_REPO, "README.md");

    List<String> readMeLines = IOUtils.readLines(new FileInputStream(readme), Charset.defaultCharset());

    StringBuilder builder = new StringBuilder(README);
    List<String> allLines = IOUtils.readLines(new FileInputStream(init), Charset.defaultCharset());
    Map<String, String> nvrams = new LinkedHashMap<>();

    //collect all nvram names from the pinemhi.ini file
    for (String line : allLines) {
      if (line.endsWith(".nv")) {
        String nvram = line.substring(line.lastIndexOf("=") + 1).trim();
        String table = line.substring(0, line.lastIndexOf("="));
        if (!nvrams.containsKey(nvram)) {
          nvrams.put(nvram, table);
        }
      }
    }

    StringBuilder indexTxt = new StringBuilder();
    for (Map.Entry<String, String> nvram : nvrams.entrySet()) {
      //create string entry and folder for the nvrams
      String readmeLine = "| " + nvram.getValue() + " | " + nvram.getKey() + " |  |  |";
      File targetFolder = new File(NVRAM_REPO, nvram.getKey() + "/");
      if (!targetFolder.exists()) {
        targetFolder.mkdirs();
      }

      File clearedNV = new File("./resources/nvrams/", nvram.getKey());
      if (clearedNV.exists()) {
        //copy file anyway
        readmeLine = readMeLines.stream().filter(l -> l.contains(nvram.getKey())).findFirst().get();
        File target = new File(targetFolder, nvram.getKey());
        if (target.exists()) {
          target.delete();
        }
        IOUtils.copy(new FileInputStream(clearedNV), new FileOutputStream(target));
        indexTxt.append(FilenameUtils.getBaseName(clearedNV.getName()));
        indexTxt.append("\n");
      }


      builder.append(readmeLine);
      builder.append("\n");
    }

    File indexTxtFile = new File( NVRAM_REPO, "index.txt");
    IOUtils.write(indexTxt, new FileOutputStream(indexTxtFile), Charset.defaultCharset());
    System.out.println("Written " + indexTxtFile.getAbsolutePath());

    System.out.println(builder.toString());
  }
}
