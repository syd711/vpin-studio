package de.mephisto.vpin.restclient.util;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import net.sf.sevenzipjbinding.util.ByteArrayStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UploaderAnalysis<T> {
  private final static Logger LOG = LoggerFactory.getLogger(UploaderAnalysis.class);
  private final static List<String> romSuffixes = Arrays.asList("bin", "rom", "cpu", "snd", "dat");
  private final static List<String> altColorSuffixes = Arrays.asList("vni", "czr", "pal", "pac", "pal");
  private final static List<String> mediaSuffixes = Arrays.asList("mp3", "png", "apng", "jpg", "mp4");
  private final static List<String> musicSuffixes = Arrays.asList("mp3", "ogg");

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

  private String readme;

  public UploaderAnalysis(File file) {
    this.file = file;
  }


  public void reset() {
    this.fileNames.clear();
    this.fileNamesWithPath.clear();
    this.directories.clear();
  }

  public File getFile() {
    return file;
  }

  public String getRomFromPupPack() {
    String contains = containsWithPath("scriptonly.txt");
    if (contains == null) {
      contains = containsWithPath(".bat");
    }
    if (contains == null) {
      contains = containsWithPath(".pup");
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

  public String getRomFromArchive() {
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
    List<String> sortedPaths = new ArrayList<>(fileNamesWithPath);
    sortedPaths.sort(new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return o1.split("/").length - o2.split("/").length;
      }
    });

    for (String fileName : sortedPaths) {
      if (fileName.toLowerCase().endsWith(s.toLowerCase())) {
        return fileName;
      }
    }
    return null;
  }

  public String getVpxFileName(String fallback) {
    String fileNameForAssetType = getFileNameForAssetType(AssetType.VPX);
    if (fileNameForAssetType == null) {
      return fallback;
    }
    return fileNameForAssetType;
  }

  public List<String> getPopperMediaFiles(VPinScreen screen) {
    String pupPackRootDirectory = getPupPackRootDirectory();
    List<String> result = new ArrayList<>();
    for (String fileNameWithPath : fileNamesWithPath) {
      if (isPopperMediaFile(screen, pupPackRootDirectory, fileNameWithPath)) {
        result.add(fileNameWithPath);
      }
    }
    return result;
  }


  public String getReadMeText() {
    return readme;
  }

  public void analyze() throws IOException {
    String suffix = FilenameUtils.getExtension(file.getName());
    if (suffix.equalsIgnoreCase(AssetType.ZIP.name())) {
      analyzeZip();
    }
    else if (suffix.equalsIgnoreCase(AssetType.RAR.name())) {
      analyzeRar();
    }
  }

  private void analyzeZip() throws IOException {
    long analysisStart = System.currentTimeMillis();
    FileInputStream fileInputStream = null;
    ZipInputStream zis = null;
    try {
      fileInputStream = new FileInputStream(file);
      zis = new ZipInputStream(fileInputStream);
      ZipEntry nextEntry = zis.getNextEntry();
      while (nextEntry != null) {
        analyze(zis, (T) nextEntry, nextEntry.getName(), nextEntry.isDirectory());
        zis.closeEntry();
        nextEntry = zis.getNextEntry();
      }
      zis.close();
      fileInputStream.close();
    } catch (Exception e) {
      LOG.error("Failed to open " + file.getAbsolutePath());
      throw e;
    } finally {
      if (fileInputStream != null) {
        fileInputStream.close();
      }
      LOG.info("Analysis finished, took " + (System.currentTimeMillis() - analysisStart) + " ms.");
    }
  }

  private void analyzeRar() throws IOException {
    long analysisStart = System.currentTimeMillis();
    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
    RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
    try {
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);
      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        analyze(inArchive, (T) item, item.getPath(), item.isFolder());
      }
      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
    } catch (Exception e) {
      LOG.error("Failed to open " + file.getAbsolutePath());
    } finally {
      randomAccessFileStream.close();
      randomAccessFile.close();
      LOG.info("Analysis finished, took " + (System.currentTimeMillis() - analysisStart) + " ms.");
    }
  }

  public void analyze(IInArchive in, T archiveEntry, String name, boolean directory) {
    String formattedName = name.replaceAll("\\\\", "/");
    boolean checkReadme = analyze(archiveEntry, formattedName, directory);
    if (checkReadme) {
      readReadme((ISimpleInArchiveItem) archiveEntry, formattedName);
    }
  }

  public void analyze(InputStream in, T archiveEntry, String name, boolean directory) {
    String formattedName = name.replaceAll("\\\\", "/");
    boolean checkReadme = analyze(archiveEntry, formattedName, directory);
    if (checkReadme) {
      readReadme(in, formattedName);
    }
  }

  public boolean analyze(T archiveEntry, String formattedName, boolean directory) {
    if (directory) {
      String[] split = formattedName.split("/");
      for (String s : split) {
        if (!StringUtils.isEmpty(s) && !directories.contains(s)) {
          directories.add(s);
        }
      }
    }
    else {
      String fileName = formattedName;
      fileNamesWithPath.add(fileName);
      if (fileName.contains("/")) {
        String dir = fileName.substring(0, fileName.lastIndexOf("/"));
        directories.add(dir);
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
      }
      fileNames.add(fileName);
      return true;
    }
    return false;
  }

  private void readReadme(InputStream in, String fileName) {
    try {
      if (fileName.toLowerCase().endsWith(".txt") && fileName.toLowerCase().contains("read")) {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        int len;
        while ((len = in.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
        this.readme = new String(fos.toByteArray());
      }
    } catch (IOException e) {
      LOG.error("Failed to extract README: " + e.getMessage(), e);
    }
  }

  private void readReadme(ISimpleInArchiveItem item, String fileName) {
    try {
      if (fileName.toLowerCase().endsWith(".txt") && fileName.toLowerCase().contains("read")) {
        ByteArrayStream fos = new ByteArrayStream(Integer.MAX_VALUE);
        item.extractSlow(fos);
        fos.close();
        this.readme = new String(fos.getBytes());
      }
    } catch (IOException e) {
      LOG.error("Failed to extract README: " + e.getMessage(), e);
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
      case RES: {
        if (hasFileWithSuffix("res")) {
          return null;
        }
        return "This archive does not not contain a .res file.";
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
        if (isMusic(true)) {
          return null;
        }
        return "This archive is not a valid music files.";
      }
      case MUSIC_BUNDLE: {
        if (isMusic(false)) {
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
        return "This archive is not a valid media pack.";
      }
      case POV: {
        if (hasFileWithSuffix("pov")) {
          return null;
        }
        return "This archive does not not contain a .pov file.";
      }
      case INI: {
        if (hasFileWithSuffixAndNot("ini", "pinup")) {
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

    if (hasFileWithSuffix("res")) {
      return AssetType.RES;
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

    if (isMusic(true)) {
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
    return getPupPackRootDirectory() != null;
  }

  private boolean isMediaPack() {
    VPinScreen[] values = VPinScreen.values();
    for (VPinScreen value : values) {
      List<String> popperMediaFiles = getPopperMediaFiles(value);
      if (!popperMediaFiles.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  private AssetType getAltColor() {
    for (String name : fileNames) {
      String suffix = FilenameUtils.getExtension(name);
      if (altColorSuffixes.contains(suffix)) {
        return AssetType.fromExtension(suffix);
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

  private boolean isMusic(boolean forceMusicFolder) {
    for (String name : fileNamesWithPath) {
      String suffix = FilenameUtils.getExtension(name);
      if (!musicSuffixes.contains(suffix)) {
        continue;
      }
      if (getPupPackRootDirectory() != null && name.startsWith(getPupPackRootDirectory())) {
        continue;
      }
      if (forceMusicFolder && name.toLowerCase().contains("music/")) {
        return true;
      }
      else {
        return true;
      }
    }
    return false;
  }

  public boolean isDMD() {
    return getDMDPath() != null;
  }


  public String getRelativeMusicPath(boolean acceptAllAudio) {
    String pupPackRootDirectory = getPupPackRootDirectory();

    for (String file : fileNamesWithPath) {
      if (pupPackRootDirectory != null && file.startsWith(pupPackRootDirectory)) {
        continue;
      }

      String suffix = FilenameUtils.getExtension(file);
      if (acceptAllAudio) {
        if (suffix.equalsIgnoreCase("ogg") || suffix.equalsIgnoreCase("mp3")) {
          if (file.contains("/")) {
            file = file.substring(0, file.lastIndexOf("/") + 1);
          }
          return file;
        }
      }

      if (!acceptAllAudio && file.toLowerCase().contains("music/")) {
        String path = file.substring(file.toLowerCase().indexOf("music/") + "music/".length());
        if (path.contains("/")) {
          path = path.substring(0, path.lastIndexOf("/") + 1);
        }
        return path;
      }
    }
    return null;
  }

  public String getDMDPath() {
    String pupPackRoot = getPupPackRootDirectory();
    for (String directory : fileNamesWithPath) {
      if (pupPackRoot != null && directory.startsWith(pupPackRoot)) {
        continue;
      }

      String[] split = directory.split("/");
      for (String segment : split) {
        if (segment.contains("UltraDMD")) {
          return segment;
        }

        if (segment.endsWith("DMD") && !segment.equalsIgnoreCase("DMD") && !segment.contains(" ")) {
          return segment;
        }
      }
    }
    return null;
  }

  public boolean isBackglass() {
    return hasFileWithSuffix("directb2s");
  }

  public boolean isRes() {
    return hasFileWithSuffix("res");
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

  private boolean hasFileWithSuffixAndNot(String s, String... exlusions) {
    for (String fileName : fileNames) {
      String suffix = FilenameUtils.getExtension(fileName);
      if (suffix.equalsIgnoreCase(s)) {
        for (String exlusion : exlusions) {
          if (fileName.toLowerCase().contains(exlusion.toLowerCase())) {
            return false;
          }
        }
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
    else {
      for (String fileNameWithPath : fileNamesWithPath) {
        if (fileNameWithPath.toLowerCase().contains("rom") && fileNameWithPath.endsWith(".zip")) {
          return true;
        }
      }

    }

    return false;
  }

  public String getPupPackRootDirectory() {
    String match = null;
    for (String name : fileNamesWithPath) {
      if (name.contains("screens.pup") || (name.toLowerCase().contains("option") && name.toLowerCase().endsWith(".bat")) || name.contains("scriptonly.txt")) {
        if (name.contains("/")) {
          String path = name.substring(0, name.lastIndexOf("/") + 1);

          //always use shortest path to exclude the options folders
          if (match == null || match.length() > path.length()) {
            match = path;
          }
        }
      }
    }
    return match;
  }

  private static boolean isPopperMediaFile(VPinScreen screen, String pupPackRootDirectory, String fileNameWithPath) {
    if (screen.equals(VPinScreen.GameInfo)) {
      return false;
    }

    if (!screen.equals(VPinScreen.Menu) && !screen.equals(VPinScreen.DMD) && fileNameWithPath.contains("DMD/")) {
      return false;
    }

    if (pupPackRootDirectory != null && fileNameWithPath.startsWith(pupPackRootDirectory)) {
      return false;
    }

    String suffix = FilenameUtils.getExtension(fileNameWithPath);
    if (!mediaSuffixes.contains(suffix)) {
      return false;
    }

    if (screen.equals(VPinScreen.AudioLaunch) && (fileNameWithPath.toLowerCase().contains("launch"))) {
      return true;
    }

    if (screen.equals(VPinScreen.Audio) && (fileNameWithPath.toLowerCase().contains("launch"))) {
      return false;
    }

    if (screen.equals(VPinScreen.GameHelp) && (fileNameWithPath.toLowerCase().contains("help") || fileNameWithPath.toLowerCase().contains("rule") || fileNameWithPath.toLowerCase().contains("card"))) {
      return true;
    }

    if (screen.equals(VPinScreen.Menu) && (fileNameWithPath.toLowerCase().contains("fulldmd") || fileNameWithPath.toLowerCase().contains("apron"))) {
      return true;
    }

    if (!fileNameWithPath.toLowerCase().contains(screen.name().toLowerCase())) {
      return false;
    }

    //ignore DMD files from DMD bundles
    if (screen.equals(VPinScreen.DMD)) {
      if (fileNameWithPath.indexOf("/") > fileNameWithPath.toLowerCase().indexOf(screen.name().toLowerCase())) {
        return false;
      }

      if (fileNameWithPath.contains("UltraDMD")) {
        return false;
      }
    }

    return true;
  }
}
