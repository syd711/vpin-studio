package de.mephisto.vpin.ui.tables;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
  private final static List<String> musicSuffixes = Arrays.asList("mp3");

  private final GameRepresentation game;
  private final File file;
  private final int total;

  private final List<String> fileNames = new ArrayList<>();
  private final List<String> directories = new ArrayList<>();

  private String error;

  public UploaderAnalysis(GameRepresentation game, File file, int total) {
    this.game = game;
    this.file = file;
    this.total = total;
  }

  public String getError() {
    return error;
  }

  public void analyze(T archiveEntry, String name, boolean directory) {
    if (directory) {
      String[] split = name.split("/");
      for (String s : split) {
        if (!StringUtils.isEmpty(s) && directories.contains(s)) {
          directories.add(s);
        }
      }
    } else {
      if (name.contains("/")) {
        name = name.substring(name.lastIndexOf("/") + 1);
      }
      fileNames.add(name);
    }
  }

  public String validateAssetType(AssetType assetType) {
    if (isMatchingRomFolderRequired(assetType)) {
      if (game == null) {
        return "A table must be selected for this upload.";
      }

      if (StringUtils.isEmpty(game.getRom())) {
        return "The table \"" + game.getGameDisplayName() + "\" has no ROM name set, but it is required for this upload type.";
      }

      if (!isMatchingRomFolderAvailable()) {
        return "The ROM name \"" + game.getRom() + "\" of table \"" + game.getGameDisplayName() + "\" was not found in this archive.";
      }
    }

    switch (assetType) {
      case ALT_SOUND: {
        if (isAltSound()) {
          return null;
        }
        return "This archive is not a valid altsound package.";
      }
      case MUSIC: {
        if (isMusic()) {
          return null;
        }
        return "This archive is not a valid music files.";
      }
      default: {
        return null;
      }
    }
  }

  public boolean isMatchingRomFolderRequired(AssetType assetType) {
    return assetType.equals(AssetType.VPX) || assetType.equals(AssetType.ALT_SOUND) || assetType.equals(AssetType.VNI) || assetType.equals(AssetType.CRZ) || assetType.equals(AssetType.PAL) || assetType.equals(AssetType.PAC);
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
    if (isVPX()) {
      return AssetType.VPX;
    }

    if (isAltSound()) {
      return AssetType.ALT_SOUND;
    }

    if (isPUPPack()) {
      return AssetType.PUP_PACK;
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

    if (audioCount > 0) {
      return true;
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


  private boolean isVPX() {
    for (String fileName : fileNames) {
      String suffix = FilenameUtils.getExtension(fileName);
      if (suffix.equalsIgnoreCase("vpx")) {
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
        } catch (Exception e) {
          //
        }
      }
    }
    return false;
  }
}
