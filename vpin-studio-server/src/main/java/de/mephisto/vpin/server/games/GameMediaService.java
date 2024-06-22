package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class GameMediaService {
  private final static Logger LOG = LoggerFactory.getLogger(GameMediaService.class);

  @Autowired
  private GameService gameService;

  public void installMediaPack(@NonNull UploadDescriptor uploadDescriptor, @Nullable UploaderAnalysis analysis) throws Exception {
    File tempFile = new File(uploadDescriptor.getTempFilename());
    if (analysis == null) {
      analysis = new UploaderAnalysis(tempFile);
      analysis.analyze();
    }

    Game game = gameService.getGame(uploadDescriptor.getGameId());
    VPinScreen[] values = VPinScreen.values();
    for (VPinScreen value : values) {
      List<String> filesForScreen = analysis.getPopperMediaFiles(value);

      boolean limit = false;
      int maxAssets = 3;
      for (String mediaFile : filesForScreen) {
        if (mediaFile.toLowerCase().contains("macosx")) {
          continue;
        }

        String suffix = FilenameUtils.getExtension(mediaFile);
        File out = uniquePopperAsset(game, value, suffix);

        if (PackageUtil.unpackTargetFile(tempFile, out, mediaFile)) {
          LOG.info("Created \"" + out.getAbsolutePath() + "\" for screen \"" + value.name() + "\" from archive file \"" + mediaFile + "\"");
        }
        else {
          LOG.error("Failed to unpack " + out.getAbsolutePath() + " from " + tempFile.getAbsolutePath());
        }

        maxAssets--;
        if (maxAssets == 0 && limit) {
          break;
        }
      }
    }
  }


  public File uniquePopperAsset(Game game, VPinScreen screen) {
    String suffix = "mp4";
    if (screen.equals(VPinScreen.AudioLaunch) || screen.equals(VPinScreen.Audio)) {
      suffix = "mp3";
    }
    return uniquePopperAsset(game, screen, suffix);
  }

  public File uniquePopperAsset(Game game, VPinScreen screen, String suffix) {
    File out = new File(game.getMediaFolder(screen), game.getGameName() + "." + suffix);
    if (out.exists()) {
      String nameIndex = "01";
      out = new File(out.getParentFile(), game.getGameName() + nameIndex + "." + suffix);
    }

    int index = 1;
    while (out.exists()) {
      index++;
      String nameIndex = index <= 9 ? "0" + index : String.valueOf(index);
      out = new File(out.getParentFile(), game.getGameName() + nameIndex + "." + suffix);
    }
    return out;
  }
}
