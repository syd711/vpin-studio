package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class GameUploadHelper {
  private final static Logger LOG = LoggerFactory.getLogger(GameUploadHelper.class);

  public static File resolveTableFilenameBasedEntry(TableUploadDescriptor tableUploadDescriptor, String suffix) throws IOException {
    File tempFile = new File(tableUploadDescriptor.getTempFilename());
    if (PackageUtil.isSupportedArchive(FilenameUtils.getExtension(tempFile.getName()))) {
      String archiveMatch = PackageUtil.contains(tempFile, suffix);
      File unpackedTempFile = File.createTempFile(FilenameUtils.getBaseName(tableUploadDescriptor.getOriginalUploadedVPXFileName()), suffix);
      PackageUtil.unpackTargetFile(tempFile, unpackedTempFile, archiveMatch);
      return unpackedTempFile;
    }
    return tempFile;
  }


  public static void importBackglass(UploaderAnalysis analysis, File temporaryUploadFile, TableUploadDescriptor uploadDescriptor, Game game) throws IOException {
    if (uploadDescriptor.isImportBackglass() && analysis.validateAssetType(AssetType.DIRECTB2S) == null) {
      File directB2SFile = game.getDirectB2SFile();
      if (directB2SFile.exists() && !directB2SFile.delete()) {
        LOG.error("Failed to delete existing backglass file " + directB2SFile.getAbsolutePath());
        throw new UnsupportedOperationException("Failed to delete existing backglass file " + directB2SFile.getAbsolutePath());
      }

      File temporaryDirectb2sFile = resolveTableFilenameBasedEntry(uploadDescriptor, ".directb2s");
      if(temporaryDirectb2sFile.exists()) {
        org.apache.commons.io.FileUtils.copyFile(temporaryUploadFile, directB2SFile);
        LOG.info("Copied \"" + temporaryDirectb2sFile.getAbsolutePath() + "\" to \"" + directB2SFile.getAbsolutePath() + "\"");
      }
    }
  }
}
