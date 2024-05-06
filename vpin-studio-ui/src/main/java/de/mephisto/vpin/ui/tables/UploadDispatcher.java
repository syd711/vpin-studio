package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class UploadDispatcher {

  public static void dispatch(@NonNull File file, GameRepresentation game) {
    String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
    switch (extension) {
      case "zip": {
        break;
      }
      case "vpx": {
        TableDialogs.openTableUploadDialog(game, TableUploadDescriptor.uploadAndImport);
        break;
      }
    }
  }
}
