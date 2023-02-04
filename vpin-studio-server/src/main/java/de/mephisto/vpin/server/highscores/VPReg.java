package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class VPReg {
  private final static Logger LOG = LoggerFactory.getLogger(VPReg.class);

  private final File vpregFile;
  private final Game game;

  public VPReg(File vpregFile, Game game) {
    this.vpregFile = vpregFile;
    this.game = game;
  }

  public boolean containsGame() {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      return getGameDirectory(root) != null;
    } catch (IOException e) {
      LOG.error("Failed to read VPReg: " + e.getMessage(), e);
    } finally {
      if (fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
    return false;
  }

  @Nullable
  public String readHighscores(HighscoreMetadata metadata) {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameFolder = getGameDirectory(root);
      if (gameFolder != null) {
        if (!gameFolder.hasEntry("Highscore1")) {
          metadata.setStatus("Found VReg entry, but no highscore entries in it.");
          return null;
        }

        StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
        int index = 1;
        String prefix = "Highscore";
        String nameSuffix = "Name";
        while (gameFolder.hasEntry(prefix + index) && gameFolder.hasEntry(prefix + index + nameSuffix)) {
          DocumentEntry scoreEntry = (DocumentEntry) gameFolder.getEntry(prefix + index);
          DocumentEntry nameEntry = (DocumentEntry) gameFolder.getEntry(prefix + index + nameSuffix);

          DocumentInputStream scoreEntryStream = new DocumentInputStream(scoreEntry);
          DocumentInputStream nameEntryStream = new DocumentInputStream(nameEntry);
          byte[] scoreContent = new byte[scoreEntryStream.available()];
          byte[] nameContent = new byte[scoreEntryStream.available()];
          scoreEntryStream.read(scoreContent);
          scoreEntryStream.close();
          nameEntryStream.read(nameContent);
          nameEntryStream.close();

          String nameString = new String(nameContent);
          String scoreString = new String(scoreContent);

          nameString = nameString.replace("\0", "").trim();
          scoreString = HighscoreResolver.formatScore(scoreString);

          builder.append("#");
          builder.append(index);
          builder.append(" ");
          builder.append(nameString);
          builder.append("   ");
          builder.append(scoreContent);
          builder.append("\n");
          index++;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          //ignore
        }
      }
      return null;
    }
  }

  /**
   * Checks if the VPReg.stg has a subfolder that matches the game's ROM name.
   *
   * @param root the root folder of the archive
   * @throws FileNotFoundException
   */
  private DirectoryEntry getGameDirectory(DirectoryEntry root) throws FileNotFoundException {
    if (root.hasEntry(game.getRom())) {
      return (DirectoryEntry) root.getEntry(game.getRom());
    }

    if (game.getTableName() != null && root.hasEntry(game.getTableName())) {
      return (DirectoryEntry) root.getEntry(game.getTableName());
    }

    return null;
  }
}
