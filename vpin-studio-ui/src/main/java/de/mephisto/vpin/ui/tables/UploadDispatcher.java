package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UploadDispatcher {
  private final static Logger LOG = LoggerFactory.getLogger(UploadDispatcher.class);

  private final static List<String> romSuffixes = Arrays.asList(".bin", ".rom", ".cpu", ".snd", ".dat");

  public static void dispatch(@NonNull TablesController tablesController, @NonNull File file, GameRepresentation game) {
    String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
    switch (extension) {
      case "zip": {
        dispatchZip(file, game);
        break;
      }
      case "directb2s": {
        TableDialogs.directBackglassUpload(Studio.stage, game, file);
        break;
      }
      case "ini": {
        TableDialogs.directIniUpload(Studio.stage, game, file);
        break;
      }
      case "pov": {
        TableDialogs.directPovUpload(Studio.stage, game, file);
        break;
      }
      case "pal": {
        TableDialogs.openAltColorUploadDialog(tablesController.getTablesSideBarController(), game, file);
        break;
      }
      case "vni": {
        TableDialogs.openAltColorUploadDialog(tablesController.getTablesSideBarController(), game, file);
        break;
      }
      case "crz": {
        TableDialogs.openAltColorUploadDialog(tablesController.getTablesSideBarController(), game, file);
        break;
      }
      case "pac": {
        TableDialogs.openAltColorUploadDialog(tablesController.getTablesSideBarController(), game, file);
        break;
      }
      case "vpx": {
        TableDialogs.openTableUploadDialog(game, TableUploadDescriptor.uploadAndImport);
        break;
      }
    }
  }

  private static void dispatchZip(File file, GameRepresentation game) {
    try {
      ProgressDialog.createProgressDialog(new ZipAnalyzer(game, file));
      System.out.println("df");
    } catch (IOException e) {
      LOG.error("Error creating UploadDispatchAnalysisZipProgressModel: " + e.getMessage(), e);
    }
  }
}
