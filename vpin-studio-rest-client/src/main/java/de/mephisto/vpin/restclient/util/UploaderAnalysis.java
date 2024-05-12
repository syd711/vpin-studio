package de.mephisto.vpin.restclient.util;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UploaderAnalysis<T> {
  private final static Logger LOG = LoggerFactory.getLogger(UploaderAnalysis.class);
  private final static List<String> romSuffixes = Arrays.asList("bin", "rom", "cpu", "snd", "dat");
  private final static List<String> altColorSuffixes = Arrays.asList("vni", "czr", "pal", "pac", "pal");
  private final static List<String> mediaSuffixes = Arrays.asList("mp3", "png", "apng", "jpg", "mp4");
  private final static List<String> musicSuffixes = Arrays.asList("mp3");

  public final static String PAL_SUFFIX = "pal";
  public final static String VNI_SUFFIX = "vni";
  public final static String PAC_SUFFIX = "pac";
  public final static String SERUM_SUFFIX = "cRZ";


  private final GameRepresentation game;
  private final File file;

  private final List<String> fileNames = new ArrayList<>();
  private final List<String> directories = new ArrayList<>();

  private String error;

  public UploaderAnalysis(GameRepresentation game, File file) {
    this.game = game;
    this.file = file;
  }

  public File getFile() {
    return file;
  }

  public String getError() {
    return error;
  }

  public String getVpxFileName() {
    for (String fileName : fileNames) {
      String suffix = FilenameUtils.getExtension(fileName);
      if (suffix.equalsIgnoreCase("vpx")) {
        return fileName;
      }
    }
    return null;
  }

  public void analyze(T archiveEntry, String name, boolean directory) {
    if (directory) {
      String[] split = name.split("/");
      for (String s : split) {
        if (!StringUtils.isEmpty(s) && !directories.contains(s)) {
          directories.add(s);
        }
      }
    }
    else {
      String fileName = name;
      if (fileName.contains("/")) {
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
      }
      fileNames.add(fileName);
    }
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
        if (isRom()) {
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
        if (isPopperMedia()) {
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

  public boolean isMatchingRomFolderRequired(AssetType assetType) {
    return assetType.equals(AssetType.ALT_SOUND) || assetType.equals(AssetType.PUP_PACK);
  }

  public boolean isMatchingRomFolderAvailable() {
    for (String directory : this.directories) {
      if (directory.equalsIgnoreCase(game.getRom())) {
        return true;
      }
    }
    return false;
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

    if (hasFileWithSuffix("pov")) {
      return AssetType.POV;
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

  private boolean isPopperMedia() {
    for (String name : fileNames) {
      String suffix = FilenameUtils.getExtension(name);
      if (mediaSuffixes.contains(suffix)) {
        return true;
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

    return audioCount > 0;
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

  private boolean isDMD() {
    for (String fileName : directories) {
      if (fileName.endsWith("DMD")) {
        return true;
      }
    }
    return false;
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
    return false;
  }
}
