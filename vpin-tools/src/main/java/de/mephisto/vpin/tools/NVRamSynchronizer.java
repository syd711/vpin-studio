package de.mephisto.vpin.tools;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * How to add a new nvram?
 * - paste the resetted nvram into ./resources/nvrams
 * - execute this class
 * - push changes of the nvrams project
 */
public class NVRamSynchronizer {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public final static File NVRAM_REPO = new File("../nvrams/");

  private final static String README = "# nvrams\n" +
      "A repository with resetted nvram files for Visual Pinball.\n" +
      "\n" +
      "List of available and missing nvrams:\n" +
      "\n" +
      "| Table | ROM | Available | Submitted By |\n" +
      "| ----- | --- | --------- |--------------|\n";

  private static String AUTHOR = "Gorgatron";

  public static void main(String[] args) throws Exception {
    synchonizeNVRamRepo();
  }

  private static void synchonizeNVRamRepo() throws Exception {
    File init = new File("./resources/pinemhi/pinemhi.ini.template");
    File readme = new File(NVRAM_REPO, "README.md");
    File allZip = new File(NVRAM_REPO, "all.zip");

    if (allZip.exists()) {
      allZip.delete();
    }

    FileOutputStream zipOutStream = new FileOutputStream(allZip);
    ZipOutputStream zipOut = new ZipOutputStream(zipOutStream);

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
        if (!readmeLine.contains("YES")) {
          readmeLine = "| " + nvram.getValue() + " | " + nvram.getKey() + " | YES | " + AUTHOR + " |";
        }
        File target = new File(targetFolder, nvram.getKey());
        if (target.exists()) {
          target.delete();
        }
        IOUtils.copy(new FileInputStream(clearedNV), new FileOutputStream(target));
        System.out.println("Copied " + target.getAbsolutePath());
        zipFile(target, target.getName(), zipOut);

        indexTxt.append(FilenameUtils.getBaseName(clearedNV.getName()));
        indexTxt.append("\n");
      }


      builder.append(readmeLine);
      builder.append("\n");
    }

    zipOut.close();

    File indexTxtFile = new File(NVRAM_REPO, "index.txt");
    IOUtils.write(indexTxt, new FileOutputStream(indexTxtFile), Charset.defaultCharset());
    System.out.println("Written " + indexTxtFile.getAbsolutePath());

    IOUtils.write(builder.toString(), new FileOutputStream(readme), Charset.defaultCharset());
    System.out.println("Written " + readme.getAbsolutePath());

  }

  private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
    if (fileToZip.isHidden()) {
      return;
    }

    if (fileToZip.isDirectory()) {
      LOG.info("Zipping " + fileToZip.getCanonicalPath());

      if (fileName.endsWith("/")) {
        zipOut.putNextEntry(new ZipEntry(fileName));
        zipOut.closeEntry();
      }
      else {
        zipOut.putNextEntry(new ZipEntry(fileName + "/"));
        zipOut.closeEntry();
      }

      File[] children = fileToZip.listFiles();
      if (children != null) {
        for (File childFile : children) {
          zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
        }
      }
      return;
    }

    FileInputStream fis = new FileInputStream(fileToZip);
    ZipEntry zipEntry = new ZipEntry(fileName);
    zipOut.putNextEntry(zipEntry);
    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zipOut.write(bytes, 0, length);
    }
    zipOut.closeEntry();
    fis.close();
  }
}
