package de.mephisto.vpin.restclient.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {
  private final static Logger LOG = LoggerFactory.getLogger(FileUtils.class);
  private final static Character[] INVALID_WINDOWS_SPECIFIC_CHARS = {'"', '*', '<', '>', '?', '|', '/', '\\', ':'};
  private final static Character[] INVALID_WINDOWS_SPECIFIC_CHARS_WITH_PATH = {'"', '*', '<', '>', '?', '|', '/', ':'};

  public static String replaceWindowsChars(String name) {
    for (Character invalidWindowsSpecificChar : INVALID_WINDOWS_SPECIFIC_CHARS) {
      if (name.contains(String.valueOf(invalidWindowsSpecificChar))) {
        name = name.replaceAll(Pattern.quote(String.valueOf(invalidWindowsSpecificChar)), "-");
      }
    }
    return name;
  }

  public static boolean isFileBelowFolder(String folder, String file) {
    String path = folder;
    if (!path.endsWith("/")) {
      path += "/";
    }

    return file.startsWith(path);
  }

  public static boolean isTempFile(File file) {
    String filename = file.getName();
    return StringUtils.endsWithIgnoreCase(filename, "tmp")
        || StringUtils.endsWithIgnoreCase(filename, "crdownload")
        || StringUtils.startsWith(filename, ".");
  }

  public static boolean deleteIfTempFile(@Nullable File file) {
    String tempDir = System.getProperty("java.io.tmpdir");
    if (file != null && file.exists() && file.getAbsolutePath().startsWith(tempDir)) {
      if (file.delete()) {
        LOG.info("Deleted temporary upload file \"" + file.getAbsolutePath() + "\"");
        return true;
      }
      else {
        LOG.error("Failed to deleted temporary upload file \"" + file.getAbsolutePath() + "\"");
      }
    }
    return false;
  }

  public static void deleteIfTempFile(@Nullable List<File> files) {
    if (files != null) {
      for (File f : files) {
        deleteIfTempFile(f);
      }
    }
  }

  public static File createMatchingTempFile(File file) throws IOException {
    File tmpFolder = new File(System.getProperty("java.io.tmpdir"));
    File tempFile = new File(tmpFolder, file.getName());
    if (tempFile.exists() && !tempFile.delete()) {
      throw new IOException("Could not delete existing temp file \"" + tempFile.getAbsolutePath() + "\"");
    }
    return tempFile;
  }

  public static void cloneFile(File original, File targetSubFolder, String updatedName) throws IOException {
    if (original.exists()) {
      String suffix = FilenameUtils.getExtension(original.getName());
      String directB2SFileName = FilenameUtils.getBaseName(updatedName) + "." + suffix;
      File clone = new File(original.getParentFile(), directB2SFileName);
      if (targetSubFolder != null) {
        clone = new File(targetSubFolder, directB2SFileName);
      }
      org.apache.commons.io.FileUtils.copyFile(original, clone);
      LOG.info("Cloned " + clone.getAbsolutePath());
    }
  }

  public static boolean renameToBaseName(File file, String name) {
    String suffix = FilenameUtils.getExtension(file.getName());
    String targetName = name + "." + suffix;
    File newFile = new File(file.getParentFile(), targetName);

    return rename(file, newFile);
  }

  public static boolean assetRename(File file, String oldBaseName, String newBaseName) {
    String suffix = FilenameUtils.getExtension(file.getName());
    String baseName = FilenameUtils.getBaseName(file.getName());

    //append the possible 01... to the base name
    String baseNameSuffix = baseName.substring(oldBaseName.length());
    String targetName = newBaseName + baseNameSuffix + "." + suffix;
    File newFile = new File(file.getParentFile(), targetName);

    return rename(file, newFile);
  }

  public static boolean delete(@Nullable File file) {
    if (file != null && file.exists()) {
      if (file.delete()) {
        LOG.info("Deleted " + file.getAbsolutePath());
      }
      else {
        LOG.warn("Failed to delete " + file.getAbsolutePath());
        return false;
      }
    }
    return true;
  }

  public static boolean rename(File file, File newFile) {
    if (file.exists()) {
      try {
        Files.createDirectories(newFile.getParentFile().toPath());
        Files.move(file.toPath(), newFile.toPath());
        LOG.info("Renamed file \"" + file.getAbsolutePath() + "\" to \"" + newFile.getAbsolutePath() + "\"");
        return true;
      }
      catch (IOException ioe) {
        LOG.warn("Renaming file \"" + file.getAbsolutePath() + "\" to \"" + newFile.getAbsolutePath() + "\" failed. " + ioe.getMessage());
        return false;
      }
    }
    LOG.warn("Renaming file \"" + file.getAbsolutePath() + "\" to \"" + newFile.getAbsolutePath() + "\" failed, the file does not exist.");
    return false;
  }

  public static boolean isEmpty(File folder) {
    if (folder.exists()) {
      File[] files = folder.listFiles();
      return files == null || files.length == 0;
    }
    return true;
  }

  public static boolean isValidFilename(@NonNull String name) {
    for (Character c : INVALID_WINDOWS_SPECIFIC_CHARS) {
      if (name.contains(String.valueOf(c))) {
        return false;
      }
    }
    return true;
  }

  public static boolean isValidFilenameWithPath(@NonNull String name) {
    for (Character c : INVALID_WINDOWS_SPECIFIC_CHARS_WITH_PATH) {
      if (name.contains(String.valueOf(c))) {
        return false;
      }
    }
    return true;
  }

  public static String readableFileSize(long size) {
    if (size <= 0) return "0";
    final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }

  public static File writeBatch(String name, String content) throws IOException {
    File path;
    if (!OSUtil.isMac()) {
      path = new File("./" + name);
    }
    else {
      path = new File(System.getProperty("MAC_WRITE_PATH") + name);
    }

    if (path.exists()) {
      path.delete();
    }
    Files.write(path.toPath(), content.getBytes());
    return path;
  }

  public static boolean deleteFolder(File folder) {
    if (folder == null || !folder.exists()) {
      return true;
    }
    try {
      org.apache.commons.io.FileUtils.deleteDirectory(folder);
    }
    catch (IOException e) {
      return false;
    }
    return true;
  }

  public static File uniqueFile(File target) {
    int index = 1;
    String originalBaseName = FilenameUtils.getBaseName(target.getName());
    String suffix = FilenameUtils.getExtension(target.getName());

    while (target.exists()) {
      target = new File(target.getParentFile(), originalBaseName + " (" + index + ")." + suffix);
      index++;
    }
    return target;
  }

  public static File uniqueFolder(File target) {
    int index = 1;
    while (target.exists() && target.isDirectory()) {
      target = new File(target.getParentFile(), target.getName() + " (" + index + ")");
      index++;
    }
    return target;
  }

  //-----------------------------------------------
  static Pattern assetPattern = Pattern.compile("\\d\\d$");

  public static boolean isDefaultAsset(String filename) {
    String basename = FilenameUtils.getBaseName(filename);
    String baseAssetName = baseUniqueAsset(filename);
    return basename.equals(baseAssetName);
  }

  public static String baseUniqueAsset(String filename) {
    String basename = FilenameUtils.removeExtension(filename).trim();
    Matcher match = assetPattern.matcher(basename);
    if (match.find()) {
      basename = match.replaceAll("");
    }
    return basename;
  }

  public static File uniqueAsset(File target) {
    int index = 1;
    String originalBaseName = FilenameUtils.getBaseName(target.getName());
    String suffix = FilenameUtils.getExtension(target.getName());

    while (target.exists()) {
      String segment = String.format("%02d", index++);
      target = new File(target.getParentFile(), originalBaseName + segment + "." + suffix);
    }
    return target;
  }

  //----------------------------------

  static Pattern filePattern = Pattern.compile(" \\(\\d\\d?\\)$");

  public static boolean equalsUniqueFile(String file1, String file2) {
    return StringUtils.equalsIgnoreCase(fromUniqueFile(file1), fromUniqueFile(file2));
  }

  /**
   * subfolder/Ace Of Speed (Original 2019) (2).directb2s => subfolder/Ace Of Speed (Original 2019).directb2s
   */
  public static String fromUniqueFile(String filename) {
    String basename = FilenameUtils.removeExtension(filename).trim();
    Matcher match = filePattern.matcher(basename);
    if (match.find()) {
      basename = match.replaceAll("");
    }
    return basename + "." + FilenameUtils.getExtension(filename);
  }

  /**
   * subfolder/Ace Of Speed (Original 2019) (2).directb2s => subfolder/Ace Of Speed (Original 2019)
   */
  public static String baseUniqueFile(String filename) {
    String basename = FilenameUtils.removeExtension(filename).trim();
    Matcher match = filePattern.matcher(basename);
    if (match.find()) {
      basename = match.replaceAll("");
    }
    return basename;
  }

  public static boolean isMainFilename(String filename) {
    return filename.equals(fromUniqueFile(filename));
  }

  public static boolean baseNameMatches(@Nullable String file1, @Nullable String file2) {
    if (file1 == null || file2 == null) {
      return false;
    }

    String base1 = FilenameUtils.getBaseName(file1);
    String base2 = FilenameUtils.getBaseName(file2);
    return base1.equalsIgnoreCase(base2);
  }

  public static void findFileRecursive(File directory, List<String> extensions, String term, List<File> result) {
    if (!directory.isDirectory()) {
      return;
    }

    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          findFileRecursive(file, extensions, term, result);
        }
        else {
          String ext = FilenameUtils.getExtension(file.getName());
          if (extensions.contains(ext.toLowerCase()) && file.getName().toLowerCase().contains(term.toLowerCase())) {
            result.add(file);
          }
        }
      }
    }
  }
}
