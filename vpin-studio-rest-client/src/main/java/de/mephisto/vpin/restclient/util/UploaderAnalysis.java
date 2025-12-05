package de.mephisto.vpin.restclient.util;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.backups.VpaArchiveUtil;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
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
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static de.mephisto.vpin.restclient.util.FileUtils.isFileBelowFolder;

public class UploaderAnalysis {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final static List<String> romSuffixes = Arrays.asList("bin", "rom", "cpu", "snd", "dat", "s2", "l1");
  private final static List<String> altColorSuffixes = Arrays.asList("vni", "crz", "pal", "pac");
  private final static List<String> mediaSuffixes = Arrays.asList("mp3", "png", "apng", "jpg", "mp4");
  private final static List<String> musicSuffixes = Arrays.asList("mp3", "ogg", "wav");

  public final static String PAL_SUFFIX = "pal";
  public final static String VNI_SUFFIX = "vni";
  public final static String PAC_SUFFIX = "pac";
  public final static String SERUM_SUFFIX = "cRZ";
  public final static String NVRAM_SUFFIX = "nv";
  public final static String FPL_SUFFIX = "fpl";
  public final static String CFG_SUFFIX = "cfg";
  public final static String BAM_CFG_SUFFIX = "cfg";

  private final File file;

  private final List<String> fileNamesWithPath = new ArrayList<>();
  private final List<String> foldersWithPath = new ArrayList<>();
  private List<String> excludedFiles = new ArrayList<>();
  private List<String> excludedFolders = new ArrayList<>();

  private String readme;
  private boolean supportPupPacks;
  private String pupFolder;

  public UploaderAnalysis(boolean supportPupPacks, File file) {
    this(supportPupPacks, file, null);
  }

  public UploaderAnalysis(boolean supportPupPacks, File file, String password) {
    this.supportPupPacks = supportPupPacks;
    this.file = file;
  }

  public boolean isArchive() {
    return PackageUtil.isSupportedArchive(FilenameUtils.getExtension(file.getName()));
  }

  public void setExclusions(List<String> excludedFiles, List<String> excludedFolders) {
    this.excludedFiles = excludedFiles;
    this.excludedFolders = excludedFolders;
  }

  public List<String> getExclusions() {
    List<String> exclusions = new ArrayList<>();
    exclusions.addAll(excludedFiles);
    exclusions.addAll(excludedFolders);
    return exclusions;
  }

  public void resetExclusions() {
    this.excludedFolders.clear();
    this.excludedFiles.clear();
  }

  public List<String> getExcludedFiles() {
    return new ArrayList<>(excludedFiles);
  }

  public List<String> getExcludedFolders() {
    return new ArrayList<>(excludedFolders);
  }

  public void reset() {
    this.fileNamesWithPath.clear();
    this.foldersWithPath.clear();
  }

  private List<String> getFilteredFilenamesWithPath() {
    List<String> result = new ArrayList<>(fileNamesWithPath);

    //filter file exclusions
    this.excludedFiles.forEach(result::remove);

    //filter all entries that are below a filtered path
    this.fileNamesWithPath.forEach(fileNameWithPath -> {
      for (String excludedFolder : excludedFolders) {
        if (isFileBelowFolder(excludedFolder, fileNameWithPath)) {
          result.remove(fileNameWithPath);
        }
      }
    });
    return result;
  }

  private List<String> getFilteredFolders() {
    List<String> result = new ArrayList<>(foldersWithPath);
    this.excludedFolders.forEach(result::remove);
    return result;
  }

  public List<String> getFileNamesWithPath() {
    return new ArrayList<>(fileNamesWithPath);
  }

  public List<String> getFoldersWithPath() {
    return new ArrayList<>(foldersWithPath);
  }

  public File getFile() {
    return file;
  }

  public String getRomFromPupPack() {
    if (!supportPupPacks) {
      return null;
    }

    String pupFolderFile = containsWithPath("scriptonly.txt");
    if (pupFolderFile == null) {
      String batPath = containsWithPath(".bat");
      String pupPath = containsWithPath(".pup");

      if (batPath != null && pupPath == null) {
        pupFolderFile = batPath;
      }
      if (batPath == null && pupPath != null) {
        pupFolderFile = pupPath;
      }

      if (batPath != null && pupPath != null) {
        String[] batSegments = batPath.split("/");
        String[] pupSegments = pupPath.split("/");
        pupFolderFile = pupSegments.length <= batSegments.length ? pupPath : batPath;
      }
    }

    if (pupFolderFile != null) {
      String rom = null;
      if (pupFolderFile.contains("/")) {
        String pupBasePath = pupFolderFile.substring(0, pupFolderFile.lastIndexOf("/"));
        pupFolder = pupBasePath;

        rom = pupBasePath;
        if (rom.contains("/")) {
          rom = rom.substring(rom.lastIndexOf("/") + 1);
        }
      }
      return rom;
    }

    return pupFolderFile;
  }

  public String getMusicFolder() {
    String path = null;
    String pupPackFolder = getPupPackRootDirectory();
    String dmdPath = getDMDPath();
    for (String filenameWithPath : getFilteredFilenamesWithPath()) {
      String suffix = FilenameUtils.getExtension(filenameWithPath);
      if (!musicSuffixes.contains(suffix)) {
        continue;
      }
      if (pupPackFolder != null && isFileBelowFolder(getPupPackRootDirectory(), filenameWithPath)) {
        continue;
      }

      if (dmdPath != null && filenameWithPath.contains(dmdPath)) {
        continue;
      }

      if (filenameWithPath.contains("/")) {
        path = filenameWithPath.substring(0, filenameWithPath.lastIndexOf("/"));
        break;
      }
    }
    return path;
  }

  public String getAltSoundFolder() {
    for (String name : getFilteredFilenamesWithPath()) {
      if (name.contains("altsound.csv") || name.contains("g-sound.csv")) {
        if (name.contains("/")) {
          return name.substring(0, name.lastIndexOf("/"));
        }
        return "/";
      }
    }
    return null;
  }

  public String getPUPPackFolder() {
    getRomFromPupPack();
    return pupFolder;
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
    List<String> sortedPaths = new ArrayList<>(getFilteredFilenamesWithPath());
    sortedPaths.sort(Comparator.comparingInt(o -> o.split("/").length));

    for (String fileName : sortedPaths) {
      if (fileName.toLowerCase().endsWith(s.toLowerCase())) {
        return fileName;
      }
    }
    return null;
  }

  public String getPatchFile() {
    for (String fileNameWithPath : getFilteredFilenamesWithPath()) {
      if (fileNameWithPath.endsWith(".dif")) {
        return fileNameWithPath;
      }
    }
    return null;
  }

  public String getTableFileName(String fallback) {
    String fileNameForAssetType = getFileNameForAssetType(AssetType.VPX);
    if (fileNameForAssetType == null) {
      fileNameForAssetType = getFileNameForAssetType(AssetType.FPT);
    }
    if (fileNameForAssetType == null) {
      return fallback;
    }
    return fileNameForAssetType;
  }

  public List<String> getPopperMediaFiles(VPinScreen screen) {
    if (!supportPupPacks) {
      return Collections.emptyList();
    }

    String pupPackRootDirectory = getPupPackRootDirectory();
    List<String> result = new ArrayList<>();
    for (String fileNameWithPath : getFilteredFilenamesWithPath()) {
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
    else if (suffix.equalsIgnoreCase(AssetType.VPA.name())) {
      analyzeVpa();
    }
    else if (suffix.equalsIgnoreCase(AssetType.RAR.name()) || suffix.equalsIgnoreCase("7z")) {
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
        analyze(zis, nextEntry, nextEntry.getName(), nextEntry.isDirectory(), nextEntry.getSize());
        zis.closeEntry();
        nextEntry = zis.getNextEntry();
      }
      zis.close();
      fileInputStream.close();
    }
    catch (Exception e) {
      LOG.error("Failed to open " + file.getAbsolutePath());
      throw e;
    }
    finally {
      if (fileInputStream != null) {
        fileInputStream.close();
      }
      LOG.info("Analysis finished, took " + (System.currentTimeMillis() - analysisStart) + " ms.");
    }
  }

  private void analyzeVpa() throws IOException {
    long analysisStart = System.currentTimeMillis();
    ZipFile zipFile = VpaArchiveUtil.createZipFile(file);
    try {
      List<FileHeader> fileHeaders = zipFile.getFileHeaders();
      for (FileHeader nextEntry : fileHeaders) {
        analyze(zipFile, nextEntry);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open " + file.getAbsolutePath());
      throw e;
    }
    finally {
      LOG.info("Analysis finished, took " + (System.currentTimeMillis() - analysisStart) + " ms.");
      zipFile.close();
    }
  }

  private void analyzeRar() throws IOException {
    long analysisStart = System.currentTimeMillis();
    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
    RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
    try {
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);
      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        analyze(inArchive, item, item.getPath(), item.isFolder(), item.getSize());
      }
      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
    }
    catch (Exception e) {
      LOG.error("Failed to open " + file.getAbsolutePath());
    }
    finally {
      randomAccessFileStream.close();
      randomAccessFile.close();
      LOG.info("Analysis finished, took " + (System.currentTimeMillis() - analysisStart) + " ms.");
    }
  }

  public void analyze(IInArchive in, ISimpleInArchiveItem archiveEntry, String name, boolean directory, long size) {
    String formattedName = name.replaceAll("\\\\", "/");
    boolean checkReadme = analyze(formattedName, directory, size);
    if (checkReadme) {
      readReadme(archiveEntry, formattedName);
    }
  }

  private void analyze(ZipFile zipFile, FileHeader fileHeader) {
    String formattedName = fileHeader.getFileName();
    boolean checkReadme = analyze(formattedName, fileHeader.isDirectory(), fileHeader.getUncompressedSize());
    if (checkReadme) {
      if (formattedName.toLowerCase().endsWith(".txt") && formattedName.toLowerCase().contains("read")) {
        try {
          this.readme = VpaArchiveUtil.readStringFromZip(zipFile, fileHeader.getFileName());
        }
        catch (Exception e) {
          //ignore
        }
      }
    }
  }

  public void analyze(InputStream in, ZipEntry archiveEntry, String name, boolean directory, long size) {
    String formattedName = name.replaceAll("\\\\", "/");
    boolean checkReadme = analyze(formattedName, directory, size);
    if (checkReadme) {
      readReadme(in, formattedName);
    }
  }

  protected boolean analyze(String formattedName, boolean directory, long size) {
    if (formattedName.contains("_MACOSX")) {
      return false;
    }
    if (formattedName.toLowerCase().contains("scorbit")) {
      return false;
    }

    if (!directory) {
      String fileName = formattedName;
      fileNamesWithPath.add(fileName);
      if (fileName.endsWith(".zip")) {
        excludedFiles.add(fileName);
      }

      if (fileName.contains("/")) {
        String dir = fileName.substring(0, fileName.lastIndexOf("/"));
        if (!StringUtils.isEmpty(dir) && !foldersWithPath.contains(dir)) {
          foldersWithPath.add(dir);
        }
      }
      return true;
    }
    return false;
  }

  private String getFileName(String fileName) {
    if (fileName.contains("/")) {
      fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
    }
    return fileName;
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
        this.readme = fos.toString();
      }
    }
    catch (IOException e) {
      LOG.error("Failed to extract README: " + e.getMessage(), e);
    }
  }

  private void readReadme(ISimpleInArchiveItem item, String fileName) {
    try {
      if (isReadMe(fileName)) {
        ByteArrayStream fos = new ByteArrayStream(Integer.MAX_VALUE);
        item.extractSlow(fos);
        fos.close();
        this.readme = new String(fos.getBytes());
      }
    }
    catch (IOException e) {
      LOG.error("Failed to extract README: " + e.getMessage(), e);
    }
  }

  public String getFileNameForAssetType(AssetType assetType) {
    for (String file : getFilteredFilenamesWithPath()) {
      String fileName = getFileName(file);
      if (AssetType.INI.equals(assetType) && fileName.equalsIgnoreCase("altsound.ini")) {
        continue;
      }
      if (fileName.toLowerCase().endsWith("." + assetType.name().toLowerCase())) {
        return fileName;
      }
    }
    return null;
  }

  public List<String> getFileNamesForAssetType(AssetType assetType) {
    if (assetType.equals(AssetType.FP_MODEL_PACK)) {
      return getFpModelPacks();
    }

    List<String> result = new ArrayList<>();
    for (String file : getFilteredFilenamesWithPath()) {
      String fileName = getFileName(file);
      if (fileName.toLowerCase().endsWith("." + assetType.name().toLowerCase())) {
        result.add(fileName);
      }
    }
    return result;
  }

  public List<String> getFileNamesForExtension(String extension) {
    List<String> result = new ArrayList<>();
    for (String file : getFilteredFilenamesWithPath()) {
      String fileName = getFileName(file);
      if (fileName.toLowerCase().endsWith("." + extension.toLowerCase())) {
        result.add(fileName);
      }
    }
    return result;
  }

  public String getFileNameWithPathForExtension(String extension) {
    for (String file : getFilteredFilenamesWithPath()) {
      if (file.toLowerCase().endsWith("." + extension.toLowerCase())) {
        return file;
      }
    }
    return null;
  }

  public String validateAssetTypeInArchive(AssetType assetType) {
    switch (assetType) {
      case VPX: {
        if (hasFileWithSuffix("vpx")) {
          return null;
        }
        return "This archive does not not contain a .vpx file.";
      }
      case FPT: {
        if (hasFileWithSuffix("fpt")) {
          return null;
        }
        return "This archive does not not contain a .fpt file.";
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
        if ((isRom() || hasFileWithSuffixAndNot("zip", "pup", "pov")) && !isFpTable()) {
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
      case FP_MODEL_PACK: {
        if (!getFpModelPacks().isEmpty()) {
          return null;
        }
        return "This archive does not not contain a Future Pinball Model bundle.";
      }
      case DIF: {
        if (hasFileWithSuffix("dif")) {
          return null;
        }
        return "This archive does not not contain a .dif file.";
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
      case FPL: {
        if (hasFileWithSuffix(FPL_SUFFIX)) {
          return null;
        }
        return "This archive does not have a .fpl file.";
      }
      case CFG: {
        if (hasFileWithSuffix(CFG_SUFFIX)) {
          return null;
        }
        return "This archive does not have a .cfg file.";
      }
      case BAM_CFG: {
        if (hasFileWithSuffix(BAM_CFG_SUFFIX)) {
          return null;
        }
        return "This archive does not have a BAM .cfg file.";
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
      case MUSIC:
      case MUSIC_BUNDLE: {
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
      case FRONTEND_MEDIA: {
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
        LOG.error("Unmapped asset type: {}", assetType + "/" + assetType.name());
        throw new UnsupportedOperationException("Unmapped asset type: " + assetType + "/" + assetType.name());
      }
    }
  }

  public List<AssetType> getAssetTypes() {
    List<AssetType> result = new ArrayList<>();
    if (hasFileWithSuffix("vpx")) {
      result.add(AssetType.VPX);
    }

    if (hasFileWithSuffix("fpt")) {
      result.add(AssetType.FPT);
    }

    if (hasFileWithSuffix("fpt") && hasFileWithSuffix("cfg")) {
      result.add(AssetType.BAM_CFG);
    }

    if (hasFileWithSuffix("fpl")) {
      result.add(AssetType.FPL);
    }

    if (hasFileWithSuffix("vpx") && hasFileWithSuffix("cfg")) {
      result.add(AssetType.CFG);
    }

    if (hasFileWithSuffix("dif")) {
      result.add(AssetType.DIF);
    }

    if (hasFileWithSuffix("cRZ")) {
      result.add(AssetType.CRZ);
    }

    if (hasFileWithSuffix("directb2s")) {
      result.add(AssetType.DIRECTB2S);
    }

    if (hasFileWithSuffix("res")) {
      result.add(AssetType.RES);
    }

    if (isAltSound()) {
      result.add(AssetType.ALT_SOUND);
    }

    if (isPUPPack()) {
      result.add(AssetType.PUP_PACK);
    }

    if (isDMD()) {
      result.add(AssetType.DMD_PACK);
    }

    if (isMusic()) {
      result.add(AssetType.MUSIC);
    }

    if (isAltColor()) {
      result.add(getAltColor());
    }

    if (isRom()) {
      result.add(AssetType.ROM);
    }

    if (isMediaPack()) {
      result.add(AssetType.FRONTEND_MEDIA);
    }

    if (hasFileWithSuffix("pov")) {
      result.add(AssetType.POV);
    }

    if (hasFileWithSuffix(NVRAM_SUFFIX)) {
      result.add(AssetType.NV);
    }

    if (hasFileWithSuffix(CFG_SUFFIX)) {
      result.add(AssetType.CFG);
    }

    if (hasFileWithSuffix("ini")) {
      result.add(AssetType.INI);
    }
    return result;
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
    for (String file : getFilteredFilenamesWithPath()) {
      String suffix = FilenameUtils.getExtension(file);
      if (altColorSuffixes.contains(suffix)) {
        return AssetType.fromExtension(null, suffix);
      }
    }
    return null;
  }

  public boolean isVpxOrFpTable() {
    String ext = FilenameUtils.getExtension(this.file.getName()).toLowerCase();
    if (ext.equalsIgnoreCase(AssetType.VPX.name()) || ext.equalsIgnoreCase(AssetType.FPT.name())) {
      return true;
    }

    return validateAssetTypeInArchive(AssetType.FPT) == null || validateAssetTypeInArchive(AssetType.VPX) == null;
  }

  public boolean isVpxTable() {
    String ext = FilenameUtils.getExtension(this.file.getName()).toLowerCase();
    if (ext.equalsIgnoreCase(AssetType.VPX.name())) {
      return true;
    }

    return validateAssetTypeInArchive(AssetType.VPX) == null;
  }

  public boolean isFpTable() {
    String ext = FilenameUtils.getExtension(this.file.getName()).toLowerCase();
    if (ext.equalsIgnoreCase(AssetType.FPT.name())) {
      return true;
    }

    return validateAssetTypeInArchive(AssetType.FPT) == null;
  }

  public boolean isPatch() {
    String ext = FilenameUtils.getExtension(this.file.getName()).toLowerCase();
    if (ext.equalsIgnoreCase(AssetType.DIF.name())) {
      return true;
    }
    return validateAssetTypeInArchive(AssetType.DIF) == null;
  }

  @Nullable
  public EmulatorType getEmulatorType() {
    String ext = FilenameUtils.getExtension(this.file.getName()).toLowerCase();
    if (validateAssetTypeInArchive(AssetType.FPT) == null || ext.equalsIgnoreCase(AssetType.FPT.name())) {
      return EmulatorType.FuturePinball;
    }
    if (validateAssetTypeInArchive(AssetType.VPX) == null || ext.equalsIgnoreCase(AssetType.VPX.name())) {
      return EmulatorType.VisualPinball;
    }
    return null;
  }

  private boolean isAltSound() {
    for (String name : getFilteredFilenamesWithPath()) {
      if (name.contains("altsound.csv") || name.contains("g-sound.csv")) {
        return true;
      }
    }
    return false;
  }

  private boolean isAltColor() {
    for (String name : getFilteredFilenamesWithPath()) {
      String suffix = FilenameUtils.getExtension(name).toLowerCase();
      if (altColorSuffixes.contains(suffix)) {
        return true;
      }
    }
    return false;
  }

  private List<String> getFpModelPacks() {
    List<String> result = new ArrayList<>();
    for (String fileName : getFilteredFilenamesWithPath()) {
      String suffix = FilenameUtils.getExtension(fileName);
      String name = FilenameUtils.getBaseName(fileName);
      if (AssetType.FPT.name().equalsIgnoreCase(suffix)) {
        String modelFile = name + ".zip";
        for (String s : getFilteredFilenamesWithPath()) {
          if (s.endsWith(modelFile)) {
            result.add(s);
            break;
          }
        }
      }
    }
    return result;
  }

  public boolean isMusic() {
    return !isAltSound() && getMusicFolder() != null;
  }

  public boolean isDMD() {
    return getDMDPath() != null;
  }


  public String getRelativeMusicPath(boolean acceptAllAudio) {
    String pupPackRootDirectory = getPupPackRootDirectory();

    for (String filenameWithPath : getFilteredFilenamesWithPath()) {
      if (pupPackRootDirectory != null && isFileBelowFolder(pupPackRootDirectory, filenameWithPath)) {
        continue;
      }

      String suffix = FilenameUtils.getExtension(filenameWithPath);

      if (suffix.equalsIgnoreCase("ogg") || suffix.equalsIgnoreCase("mp3")) {
        if (acceptAllAudio) {
          if (filenameWithPath.contains("/")) {
            filenameWithPath = filenameWithPath.substring(0, filenameWithPath.lastIndexOf("/") + 1);
          }
          return filenameWithPath;
        }

        if (filenameWithPath.toLowerCase().contains("music/")) {
          String path = filenameWithPath.substring(filenameWithPath.toLowerCase().indexOf("music/") + "music/".length());
          if (path.contains("/")) {
            path = path.substring(0, path.lastIndexOf("/") + 1);
          }
          return path;
        }
        else if (filenameWithPath.contains("/")) {
          String path = filenameWithPath.substring(0, filenameWithPath.lastIndexOf("/") + 1);
          while (StringUtils.countMatches(path, "/") > 1) {
            path = path.substring(path.indexOf("/") + 1);
          }
          return path;
        }
        else {
          return "/";
        }
      }
    }
    return null;
  }

  public String getDMDPath() {
    String pupPackRoot = getPupPackRootDirectory();
    for (String filenameWithPath : getFilteredFolders()) {
      if (pupPackRoot != null && isFileBelowFolder(pupPackRoot, filenameWithPath)) {
        continue;
      }

      String[] split = filenameWithPath.split("/");
      for (String segment : split) {
        if (segment.contains("UltraDMD") || segment.contains("FlexDMD")) {
          return filenameWithPath.substring(0, filenameWithPath.indexOf(segment) + segment.length());
        }

        if ((segment.endsWith("DMD") || segment.toLowerCase().endsWith(".flex"))
            && !segment.equalsIgnoreCase("DMD")
            && !segment.equalsIgnoreCase("FullDMD")
            && !segment.contains(" ")) {
          return filenameWithPath;
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
    for (String fileName : getFilteredFilenamesWithPath()) {
      String suffix = FilenameUtils.getExtension(fileName);
      if (suffix.equalsIgnoreCase(s) && !fileName.toLowerCase().endsWith("altsound.ini")) {
        return true;
      }
    }
    return false;
  }

  private boolean hasFileWithSuffixAndNot(String s, String... exlusions) {
    for (String file : getFilteredFilenamesWithPath()) {
      String fileName = getFileName(file);
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
    if (isFpTable()) {
      return false;
    }

    if (getFilteredFolders().isEmpty()) {
      for (String fileName : getFilteredFilenamesWithPath()) {
        String suffix = FilenameUtils.getExtension(fileName);
        if (romSuffixes.contains(suffix.toLowerCase())) {
          return true;
        }
        if (suffix.length() == 2) {
          try {
            String s = suffix.substring(suffix.length() - 1);
            Integer.parseInt(s);
            return true;
          }
          catch (Exception e) {
            //ignore
          }
        }
      }

      //some rom names only contains files with numeric endings.
      for (String fileName : getFilteredFilenamesWithPath()) {
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
      for (String fileNameWithPath : getFilteredFilenamesWithPath()) {
        if (fileNameWithPath.toLowerCase().contains("rom") && fileNameWithPath.endsWith(".zip")) {
          return true;
        }
      }
    }

    return false;
  }

  public String getPupPackRootDirectory() {
    String match = null;
    for (String name : getFilteredFilenamesWithPath()) {
      if (name.contains("screens.pup")
          || (name.toLowerCase().contains("option") && name.toLowerCase().endsWith(".bat"))
          || name.contains("EditThisPuPPack.bat")
          || (name.toLowerCase().contains("screen") && name.toLowerCase().endsWith(".bat"))
          || name.contains("scriptonly.txt")) {
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

  public byte[] readFile(String name) {
    return PackageUtil.readFile(file, name);
  }

  private boolean isReadMe(String fileName) {
    if (fileName.toLowerCase().endsWith(".txt")) {
      if (fileName.toLowerCase().contains("readme") || fileName.toLowerCase().contains("read me")) {
        return true;
      }

      return !fileName.contains("/");
    }
    return false;
  }

  private static boolean isPopperMediaFile(VPinScreen screen, String pupPackRootDirectory, String fileNameWithPath) {
    if (fileNameWithPath.toLowerCase().contains("screenshot")) {
      return false;
    }

    if (!screen.equals(VPinScreen.Menu) && !screen.equals(VPinScreen.DMD) && fileNameWithPath.contains("DMD/")) {
      return false;
    }

    if (pupPackRootDirectory != null && isFileBelowFolder(pupPackRootDirectory, fileNameWithPath)) {
      return false;
    }

    String suffix = FilenameUtils.getExtension(fileNameWithPath);
    if (!mediaSuffixes.contains(suffix.toLowerCase())) {
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

    if (screen.equals(VPinScreen.GameInfo) && fileNameWithPath.toLowerCase().contains("flyer")) {
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
      return !fileNameWithPath.contains("UltraDMD");
    }

    return true;
  }

}
