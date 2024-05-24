package de.mephisto.vpin.restclient.util;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UploaderAnalysis<T> {
  private final static Logger LOG = LoggerFactory.getLogger(UploaderAnalysis.class);
  private final static List<String> romSuffixes = Arrays.asList("bin", "rom", "cpu", "snd", "dat");
  private final static List<String> altColorSuffixes = Arrays.asList("vni", "czr", "pal", "pac", "pal");
  private final static List<String> mediaSuffixes = Arrays.asList("mp3", "png", "apng", "jpg", "mp4");
  private final static List<String> musicSuffixes = Arrays.asList("mp3");
  private final static List<String> mediaScreenNames = Arrays.stream(PopperScreen.values()).map(s -> s.name().toLowerCase()).collect(Collectors.toList());


  public final static String PAL_SUFFIX = "pal";
  public final static String VNI_SUFFIX = "vni";
  public final static String PAC_SUFFIX = "pac";
  public final static String SERUM_SUFFIX = "cRZ";
  public final static String NVRAM_SUFFIX = "nv";
  public final static String CFG_SUFFIX = "cfg";

  private final File file;

  private final List<String> fileNames = new ArrayList<>();
  private final List<String> fileNamesWithPath = new ArrayList<>();
  private final List<String> directories = new ArrayList<>();

  private String error;

  public UploaderAnalysis(File file) {
    this.file = file;
  }

  public File getFile() {
    return file;
  }

  public String getError() {
    return error;
  }

  public String getRomFromPupPack() {
    String contains = containsWithPath(".pup");
    if (contains == null) {
      contains = containsWithPath(".bat");
    }
    if (contains == null) {
      contains = containsWithPath(".txt");
    }
    if (contains != null) {
      String rom = contains;
      if (rom.contains("/")) {
        rom = rom.substring(0, rom.lastIndexOf("/"));
        if (rom.contains("/")) {
          rom = rom.substring(rom.lastIndexOf("/") + 1);
        }
      }
      LOG.info("Resolved archive ROM: " + rom);
      return rom;
    }

    return contains;
  }

  public String getRomFromAltSoundPack() {
    for (String name : fileNamesWithPath) {
      if (name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".csv") || name.contains("altsound.csv") || name.contains("g-sound.csv")) {
        if (name.contains("/")) {
          name = name.substring(0, name.lastIndexOf("/"));
          if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
          }
          return name;
        }
      }
    }
    return null;
  }

  public String getRomFromZip() {
    String contains = containsWithPath(".zip");
    if (contains != null) {
      String rom = contains;
      if (rom.contains("/")) {
        rom = rom.substring(rom.lastIndexOf("/") + 1);
      }
      rom = FilenameUtils.getBaseName(rom);
      LOG.info("Resolved archive ROM: " + rom);
      return rom;
    }

    return contains;
  }

  private String containsWithPath(String s) {
    for (String fileName : fileNamesWithPath) {
      if (fileName.endsWith(s)) {
        return fileName;
      }
    }
    return null;
  }

  public String getVpxFileName() {
    return getFileNameForAssetType(AssetType.VPX);
  }

  public List<String> getPopperMediaFiles(PopperScreen screen) {
    List<String> result = new ArrayList<>();
    for (String fileNameWithPath : fileNamesWithPath) {
      String suffix = FilenameUtils.getExtension(fileNameWithPath);
      if (mediaSuffixes.contains(suffix)) {
        if (fileNameWithPath.toLowerCase().contains(screen.name().toLowerCase())) {
          result.add(fileNameWithPath);
        }
      }
    }
    return result;
  }

  public void analyze() throws IOException {
    long analysisStart = System.currentTimeMillis();
    FileInputStream fileInputStream = null;
    ZipInputStream zis = null;
    try {
      fileInputStream = new FileInputStream(file);
      zis = new ZipInputStream(fileInputStream);
      ZipEntry nextEntry = zis.getNextEntry();
      while (nextEntry != null) {
        analyze((T) nextEntry, nextEntry.getName(), nextEntry.isDirectory());
        zis.closeEntry();
        nextEntry = zis.getNextEntry();
      }
      zis.close();
      fileInputStream.close();
    }
    catch (Exception e) {
      LOG.error("Failed to open " + file.getAbsolutePath());
      throw e;
    } finally {
      if (fileInputStream != null) {
        fileInputStream.close();
      }
      LOG.info("Analysis finished, took " + (System.currentTimeMillis() - analysisStart) + " ms.");
    }
  }

  public void analyze(T archiveEntry, String name, boolean directory) {
    if (directory) {
      String[] split = name.replaceAll("\\\\", "/").split("/");
      for (String s : split) {
        if (!StringUtils.isEmpty(s) && !directories.contains(s)) {
          directories.add(s);
        }
      }
    }
    else {
      String fileName = name.replaceAll("\\\\", "/");
      fileNamesWithPath.add(fileName);
      if (fileName.contains("/")) {
        String dir = fileName.substring(0, fileName.lastIndexOf("/"));
        directories.add(dir);
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
      }
      fileNames.add(fileName);
    }
  }

  public String getFileNameForAssetType(AssetType assetType) {
    for (String fileName : fileNames) {
      if (fileName.toLowerCase().endsWith("." + assetType.name().toLowerCase())) {
        return fileName;
      }
    }
    return null;
  }

  public String validateAssetType(AssetType assetType) {
    switch (assetType) {
      case VPX: {
        if (hasFileWithSuffix("vpx")) {
          return null;
        }
        return "This archive does not not contain a .vpx file.";
      }
      case DIRECTB2S: {
        if (hasFileWithSuffix("directb2s")) {
          return null;
        }
        return "This archive does not not contain a .directb2s file.";
      }
      case ROM: {
        if (isRom() || hasFileWithSuffix("zip")) {
          return null;
        }
        return "This archive does not not contain a ROM file.";
      }
      case DMD_PACK: {
        if (isDMD()) {
          return null;
        }
        return "This archive does not not contain a DMD bundle.";
      }
      case ALT_SOUND: {
        if (isAltSound()) {
          return null;
        }
        return "This archive is not a valid ALT sound package.";
      }
      case NV: {
        if (hasFileWithSuffix(NVRAM_SUFFIX)) {
          return null;
        }
        return "This archive does not have a .nv file.";
      }
      case CFG: {
        if (hasFileWithSuffix(CFG_SUFFIX)) {
          return null;
        }
        return "This archive does not have a .cfg file.";
      }
      case ALT_COLOR:
      case PAC:
      case VNI:
      case CRZ:
      case PAL: {
        if (isAltColor()) {
          return null;
        }
        return "This archive is not a valid ALT color package.";
      }
      case MUSIC: {
        if (isMusic()) {
          return null;
        }
        return "This archive is not a valid music files.";
      }
      case PUP_PACK: {
        if (isPUPPack()) {
          return null;
        }
        return "This archive is not a valid PUP pack.";
      }
      case POPPER_MEDIA: {
        if (isMediaPack()) {
          return null;
        }
        return "This archive is not a valid PUP pack.";
      }
      case POV: {
        if (hasFileWithSuffix("pov")) {
          return null;
        }
        return "This archive does not not contain a .pov file.";
      }
      case INI: {
        if (hasFileWithSuffix("ini")) {
          return null;
        }
        return "This archive does not not contain a .ini file.";
      }
      default: {
        throw new UnsupportedOperationException("Unmapped asset type: " + assetType);
      }
    }
  }

  public AssetType getSingleAssetType() {
    if (hasFileWithSuffix("vpx")) {
      return AssetType.VPX;
    }

    if (hasFileWithSuffix("directb2s")) {
      return AssetType.DIRECTB2S;
    }

    if (isAltSound()) {
      return AssetType.ALT_SOUND;
    }

    if (isPUPPack()) {
      return AssetType.PUP_PACK;
    }

    if (isDMD()) {
      return AssetType.DMD_PACK;
    }

    if (isMusic()) {
      return AssetType.MUSIC;
    }

    if (isAltColor()) {
      return getAltColor();
    }

    if (isRom()) {
      return AssetType.ROM;
    }

    if (isMediaPack()) {
      return AssetType.POPPER_MEDIA;
    }

    if (hasFileWithSuffix("pov")) {
      return AssetType.POV;
    }

    if (hasFileWithSuffix(NVRAM_SUFFIX)) {
      return AssetType.NV;
    }

    if (hasFileWithSuffix(CFG_SUFFIX)) {
      return AssetType.CFG;
    }

    if (hasFileWithSuffix("ini")) {
      return AssetType.INI;
    }
    return null;
  }

  private boolean isPUPPack() {
    for (String name : fileNames) {
      if (name.contains("screens.pup") || name.contains("scriptonly.txt")) {
        return true;
      }
    }
    return false;
  }

  private boolean isMediaPack() {
    for (String fileNameWithPath : fileNamesWithPath) {
      String suffix = FilenameUtils.getExtension(fileNameWithPath);
      if (mediaSuffixes.contains(suffix)) {
        for (String mediaScreenName : mediaScreenNames) {
          if (fileNameWithPath.toLowerCase().contains(mediaScreenName)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private AssetType getAltColor() {
    for (String name : fileNames) {
      String suffix = FilenameUtils.getExtension(name);
      if (altColorSuffixes.contains(suffix)) {
        return AssetType.valueOf(suffix.toUpperCase());
      }
    }
    return null;
  }

  private boolean isAltSound() {
    int audioCount = 0;
    for (String name : fileNames) {
      if (name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".csv")) {
        audioCount++;
      }
      if (name.contains("altsound.csv") || name.contains("g-sound.csv")) {
        return true;
      }
    }
    return false;
  }

  private boolean isAltColor() {
    for (String name : fileNames) {
      String suffix = FilenameUtils.getExtension(name);
      if (altColorSuffixes.contains(suffix)) {
        return true;
      }
    }
    return false;
  }

  private boolean isMusic() {
    for (String name : fileNames) {
      String suffix = FilenameUtils.getExtension(name);
      if (musicSuffixes.contains(suffix)) {
        return true;
      }
    }
    return false;
  }

  public boolean isDMD() {
    for (String fileName : directories) {
      String[] split = fileName.split("/");
      for (String segment : split) {
        if (segment.endsWith("DMD") && !segment.equalsIgnoreCase("DMD")) {
          return true;
        }
      }
    }

    int count = 0;
    for (String fileName : fileNamesWithPath) {
      if (fileName.contains("DMD/") && !fileName.contains("/DMD/")) {
        count++;
      }
    }
    return count > 1; //TODO dunno
  }


  public boolean isBackglass() {
    return hasFileWithSuffix("directb2s");
  }

  private boolean hasFileWithSuffix(String s) {
    for (String fileName : fileNames) {
      String suffix = FilenameUtils.getExtension(fileName);
      if (suffix.equalsIgnoreCase(s)) {
        return true;
      }
    }
    return false;
  }

  private boolean isRom() {
    if (directories.isEmpty()) {
      for (String fileName : fileNames) {
        String suffix = FilenameUtils.getExtension(fileName);
        if (romSuffixes.contains(suffix.toLowerCase())) {
          return true;
        }
      }

      //some rom names only contains files with numeric endings.
      for (String fileName : fileNames) {
        String suffix = FilenameUtils.getExtension(fileName);
        try {
          Integer.parseInt(suffix);
          return true;
        }
        catch (Exception e) {
          //
        }
      }
    }
    else {
      for (String fileNameWithPath : fileNamesWithPath) {
        if (fileNameWithPath.toLowerCase().contains("rom") && fileNameWithPath.endsWith(".zip")) {
          return true;
        }
      }

    }

    return false;
  }
}
