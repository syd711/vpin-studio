package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.GameFileType;
import de.mephisto.vpin.restclient.system.FileInfo;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the files and folders of a table that the client opens in the file explorer.
 * <p>
 * The client can't build these paths itself, as they depend on the file layout of the table: the shared folders of
 * the installation or the VPX 10.8.1 per table layout, with the table in its own subfolder. The fallback of each
 * {@link FileInfo} is an existing folder, so there is always something to open, also through a network share where
 * the client can't check whether a path exists.
 */
@Service
public class GameFileInfoService {

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DMDService dmdService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private FolderLookupService folderLookupService;

  @Autowired
  private SystemService systemService;

  @Nullable
  public FileInfo getFileInfo(@NonNull Game game, @NonNull GameFileType type) {
    File gameFolder = game.getGameFolder();
    return switch (type) {
      case TABLE -> file(game.getGameFile(), gameFolder);
      case DIRECTB2S -> file(game.getDirectB2SFile(), gameFolder);
      case POV -> file(game.getPOVFile(), gameFolder);
      case DMD -> StringUtils.isEmpty(game.getDMDProjectFolder()) ? null : folder(dmdService.getDmdFolder(game), gameFolder);
      case PUPPACK -> folder(getPupPackFolder(game), getPupVideosFolder());
      case HIGHSCORE -> getHighscoreFileInfo(game);
    };
  }

  @Nullable
  private FileInfo getHighscoreFileInfo(@NonNull Game game) {
    File hsFile = highscoreService.getHighscoreFile(game);
    if (hsFile != null) {
      return file(hsFile, hsFile.getParentFile());
    }
    // the fpRAM file only exists once the table has been played
    if (game.isFpGame() && game.getEmulator() != null) {
      File installationFolder = game.getEmulator().getInstallationFolder();
      return folder(new File(installationFolder, "fpRAM"), installationFolder);
    }
    return null;
  }

  /**
   * VPX 10.8.1 reads the PUP pack from the pupvideos folder next to the table, which wins over the PUPVideos folder of
   * the PinUP Player installation.
   */
  @NonNull
  private File getPupPackFolder(@NonNull Game game) {
    List<String> names = new ArrayList<>();
    for (String name : new String[]{game.getPupPackName(), game.getRomAlias(), game.getRom()}) {
      if (!StringUtils.isEmpty(name)) {
        names.add(name);
      }
    }

    if (folderLookupService.isPerTableFileStructure(game)) {
      File tablePupVideos = new File(game.getGameFolder(), "pupvideos");
      File pupFolder = findFolder(tablePupVideos, names);
      if (pupFolder != null) {
        return pupFolder;
      }
      if (tablePupVideos.exists()) {
        return tablePupVideos;
      }
    }

    PupPack pupPack = pupPacksService.getPupPackCached(game);
    if (pupPack != null && pupPack.getPupPackFolder() != null) {
      return pupPack.getPupPackFolder();
    }

    File pupVideosFolder = getPupVideosFolder();
    File pupFolder = findFolder(pupVideosFolder, names);
    return pupFolder != null ? pupFolder : pupVideosFolder;
  }

  @Nullable
  private static File findFolder(@NonNull File parent, @NonNull List<String> names) {
    for (String name : names) {
      File folder = new File(parent, name);
      if (folder.isDirectory()) {
        return folder;
      }
    }
    return null;
  }

  @NonNull
  private File getPupVideosFolder() {
    return new File(systemService.getPinupInstallationFolder(), "PUPVideos");
  }

  //-------------------------------- FileInfo factories --------------------------------

  /**
   * A file, with the closest existing folder of the given fallback (or of the file) as fallback.
   */
  @Nullable
  public static FileInfo file(@Nullable File file, @Nullable File fallback) {
    if (file == null) {
      return null;
    }
    File existingFallback = getExistingFolder(fallback != null ? fallback : file.getParentFile());
    return FileInfo.file(file, existingFallback);
  }

  /**
   * A folder, replaced by the fallback (or its closest existing parent) if it doesn't exist, as a client that opens
   * it through a network share can't check this.
   */
  @Nullable
  public static FileInfo folder(@Nullable File folder, @Nullable File fallback) {
    File existingFallback = getExistingFolder(fallback);
    if (folder == null) {
      return existingFallback != null ? FileInfo.folder(existingFallback, null) : null;
    }
    if (folder.exists()) {
      return FileInfo.folder(folder, existingFallback);
    }
    File existing = existingFallback != null ? existingFallback : getExistingFolder(folder);
    return FileInfo.folder(existing != null ? existing : folder, existingFallback);
  }

  @Nullable
  static File getExistingFolder(@Nullable File folder) {
    while (folder != null && !folder.exists()) {
      folder = folder.getParentFile();
    }
    return folder;
  }
}
