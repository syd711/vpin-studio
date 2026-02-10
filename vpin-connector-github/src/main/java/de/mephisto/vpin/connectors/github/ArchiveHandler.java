package de.mephisto.vpin.connectors.github;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArchiveHandler {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @NonNull
  private final File archiveFile;

  @NonNull
  private final ReleaseArtifactActionLog installLog;

  @NonNull
  private final List<String> excludedFiles;

  @NonNull
  private final List<String> includedFiles;

  private List<String> rootFileIndicators;

  private boolean diff = false;
  private boolean simulate = false;

  public ArchiveHandler(@NonNull File archiveFile, @NonNull ReleaseArtifactActionLog installLog, @NonNull List<String> rootFileIndicators, @NonNull List<String> excludedFiles, @NonNull List<String> includedFiles) {
    this.archiveFile = archiveFile;
    this.installLog = installLog;
    this.rootFileIndicators = rootFileIndicators;
    this.includedFiles = includedFiles;
    this.excludedFiles = excludedFiles.stream().map(f -> f.toLowerCase()).collect(Collectors.toList());
  }

  public void diff(@NonNull File destinationDir) {
    this.diff = true;
    this.simulate = true;
    run(destinationDir);
  }

  public void simulate(@NonNull File destinationDir) {
    this.diff = false;
    this.simulate = true;
    run(destinationDir);
  }

  public void unzip(@NonNull File destinationDir) {
    this.diff = false;
    this.simulate = false;
    run(destinationDir);
  }

  private void run(@NonNull File destinationDir) {
    try {
      if (simulate) {
        installLog.log("Simulating extraction of \"" + archiveFile.getName() + "\" to \"" + destinationDir.getAbsolutePath() + "\"");
      }
      else {
        installLog.log("Extracting \"" + archiveFile.getName() + "\" to \"" + destinationDir.getAbsolutePath() + "\"");
      }

      boolean skipRoot = isRootSkipped();

      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        String name = zipEntry.getName();
        if (skipRoot && name.contains("/")) {
          name = name.substring(name.indexOf("/"));
        }

        File newFile = new File(destinationDir, name);

        if (zipEntry.isDirectory() || name.endsWith("\\")) {
          checkDirectory(newFile);
        }
        else {
          // fix for Windows-created archives
          File parent = newFile.getParentFile();
          if (!simulate && !parent.exists() && !newFile.mkdirs()) {
            installLog.setStatus("Failed to create directory " + parent.getAbsolutePath());
            throw new IOException("Failed to create directory " + parent.getAbsolutePath());
          }

          if (checkFile(zipEntry, zis, newFile)) {
            unzipFile(newFile, zis, buffer);
          }
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    }
    catch (Exception e) {
      installLog.setStatus("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage());
      throw new UnsupportedOperationException("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
  }

  private boolean isRootSkipped() {
    boolean doSkip = false;
    try {
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        String name = zipEntry.getName();
        for (String rootFileIndicator : rootFileIndicators) {
          if (name.contains(rootFileIndicator) && name.contains("/")) {
            doSkip = true;
          }
        }

        zis.closeEntry();
        if (doSkip) {
          break;
        }

        zipEntry = zis.getNextEntry();
      }

      zis.closeEntry();
      zis.close();
      fileInputStream.close();
    }
    catch (IOException e) {
      LOG.error("Error determining root folder of \"" + archiveFile.getAbsolutePath() + "\": " + e.getMessage(), e);
    }
    return doSkip;
  }

  private void checkDirectory(File newFile) throws IOException {
    if (!simulate && !newFile.exists() && !newFile.mkdirs()) {
      installLog.setStatus("Failed to create directory " + newFile.getAbsolutePath());
      throw new IOException("Failed to create directory " + newFile.getAbsolutePath());
    }

    if (diff) {
      if (!newFile.exists()) {
        installLog.addDiffEntry(newFile.getAbsolutePath(), DiffState.TARGET_FOLDER_NOT_EXIST, -1, -1);
      }
    }
  }

  private boolean checkFile(ZipEntry entry, ZipInputStream zis, File newFile) throws IOException {
    if (isExcluded(entry.getName())) {
      if (simulate || newFile.exists() || diff) {
        installLog.log("Skipped excluded file " + entry.getName());
        installLog.addDiffEntry(newFile.getAbsolutePath(), DiffState.SKIPPED, -1, -1);
        return false;
      }
    }

    if (newFile.exists()) {
      if (simulate && !newFile.canWrite()) {
        installLog.setStatus("Failed to delete file: " + newFile.getAbsolutePath());
        throw new UnsupportedOperationException("Failed to delete file: " + newFile.getAbsolutePath());
      }
      if (!simulate && !newFile.delete()) {
        installLog.setStatus("Failed to delete file: " + newFile.getAbsolutePath());
        throw new UnsupportedOperationException("Failed to delete file: " + newFile.getAbsolutePath());
      }

      if (simulate) {
        installLog.log("Simulating overwrite of " + newFile.getAbsolutePath());
        LOG.debug("Simulating overwriting of " + newFile.getAbsolutePath());
      }
      else {
        installLog.log("Overwriting " + newFile.getAbsolutePath());
        LOG.debug("Overwriting " + newFile.getAbsolutePath());
      }
    }
    else {
      if (simulate) {
        installLog.log("Simulating write of " + newFile.getAbsolutePath());
        LOG.debug("Simulating writing of " + newFile.getAbsolutePath());
      }
      else {
        installLog.log("Writing " + newFile.getAbsolutePath());
        LOG.debug("Writing " + newFile.getAbsolutePath());
      }
    }


    if (diff) {
      if (newFile.exists()) {
        long zipSize = entry.getSize();
        long fileSize = newFile.length();

        if (zipSize == -1) {
          byte[] buffer = new byte[1024];
          File tempFile = File.createTempFile(newFile.getName(), ".tmp");
          tempFile.deleteOnExit();
          doUnzip(tempFile, zis, buffer);
          zipSize = tempFile.length();
          tempFile.delete();
        }

        if (zipSize != fileSize) {
          installLog.addDiffEntry(newFile.getAbsolutePath(), DiffState.SIZE_DIFF, fileSize, zipSize);
        }
        else {
          installLog.addDiffEntry(newFile.getAbsolutePath(), DiffState.FILE_MATCH, fileSize, zipSize);
        }
      }
      else {
        installLog.addDiffEntry(newFile.getAbsolutePath(), DiffState.TARGET_FILE_NOT_EXIST, -1, -1);
      }
    }

    return true;
  }

  private boolean isExcluded(String name) {
    for (String includedFile : includedFiles) {
      if (name.contains(includedFile)) {
        return false;
      }
    }

    if (this.excludedFiles.contains(name.toLowerCase())) {
      return true;
    }

    for (String excludedFile : this.excludedFiles) {
      if (excludedFile.startsWith(".") && name.toLowerCase().endsWith(excludedFile)) {
        return true;
      }
    }
    return false;
  }

  private void unzipFile(File newFile, ZipInputStream zis, byte[] buffer) throws IOException {
    if (!simulate) {
      doUnzip(newFile, zis, buffer);
    }
  }

  private void doUnzip(File newFile, ZipInputStream zis, byte[] buffer) throws IOException {
    FileOutputStream fos = new FileOutputStream(newFile);
    int len;
    while ((len = zis.read(buffer)) > 0) {
      fos.write(buffer, 0, len);
    }
    fos.close();
  }
}
