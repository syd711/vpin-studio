package de.mephisto.vpin.server.util.vpreg;

import com.thoughtworks.xstream.core.util.Base64Encoder;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.poifs.filesystem.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class VPReg {
  public static final String NAME_SUFFIX = "Name";
  private final static Logger LOG = LoggerFactory.getLogger(VPReg.class);
  public static final String HIGH_SCORE = "HighScore";

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
    } catch (Exception e) {
      LOG.error("Failed to read VPReg: " + e.getMessage());
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

  public boolean restoreHighscore(VPRegScoreSummary summary) {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameFolder = getGameDirectory(root);
      if (gameFolder != null) {
        if (!gameFolder.hasEntry(HIGH_SCORE + "1")) {
          return false;
        }

        LOG.info("Writing VPReg entry \"" + gameFolder.getName() + "\"");
        List<VPRegScoreEntry> scores = summary.getScores();
        for (VPRegScoreEntry score : scores) {
          DocumentNode scoreEntry = (DocumentNode) gameFolder.getEntry(HIGH_SCORE + score.getPos());
          POIFSDocument scoreDocument = new POIFSDocument(scoreEntry);
          scoreDocument.replaceContents(new ByteArrayInputStream(new Base64Encoder().decode(score.getBase64Score())));

          DocumentNode nameEntry = (DocumentNode) gameFolder.getEntry(HIGH_SCORE + score.getPos() + NAME_SUFFIX);
          POIFSDocument nameDocument = new POIFSDocument(nameEntry);
          nameDocument.replaceContents(new ByteArrayInputStream(new Base64Encoder().decode(score.getBase64Name())));

          LOG.info("Written VPReg score entry: " + score);
        }

        fs.writeFilesystem();
        return true;
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
    }
    return false;
  }

  public boolean resetHighscores() {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameFolder = getGameDirectory(root);
      if (gameFolder != null) {
        if (!gameFolder.hasEntry(HIGH_SCORE + "1")) {
          return false;
        }
        int index = 1;
        while (gameFolder.hasEntry(HIGH_SCORE + index) && gameFolder.hasEntry(HIGH_SCORE + index + NAME_SUFFIX)) {
          DocumentNode scoreEntry = (DocumentNode) gameFolder.getEntry(HIGH_SCORE + index);
          POIFSDocument scoreDocument = new POIFSDocument(scoreEntry);
          scoreDocument.replaceContents(new ByteArrayInputStream("0".getBytes()));

          DocumentNode nameEntry = (DocumentNode) gameFolder.getEntry(HIGH_SCORE + index + NAME_SUFFIX);
          POIFSDocument nameDocument = new POIFSDocument(nameEntry);
          nameDocument.replaceContents(new ByteArrayInputStream("???".getBytes()));

          index++;
        }

        fs.writeFilesystem();
        return true;
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
    }
    return false;
  }

  @Nullable
  public VPRegScoreSummary readHighscores() {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, true);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameFolder = getGameDirectory(root);
      if (gameFolder != null) {
        if (!gameFolder.hasEntry(HIGH_SCORE + "1")) {
          return null;
        }

        VPRegScoreSummary summary = new VPRegScoreSummary();
        int index = 1;
        String prefix = HIGH_SCORE;
        String nameSuffix = "Name";
        while (gameFolder.hasEntry(prefix + index) && gameFolder.hasEntry(prefix + index + nameSuffix)) {
          DocumentEntry scoreEntry = (DocumentEntry) gameFolder.getEntry(prefix + index);
          DocumentEntry nameEntry = (DocumentEntry) gameFolder.getEntry(prefix + index + nameSuffix);

          DocumentInputStream scoreEntryStream = new DocumentInputStream(scoreEntry);
          byte[] scoreContent = new byte[scoreEntryStream.available()];
          scoreEntryStream.read(scoreContent);
          scoreEntryStream.close();

          DocumentInputStream nameEntryStream = new DocumentInputStream(nameEntry);
          byte[] nameContent = new byte[nameEntryStream.available()];
          nameEntryStream.read(nameContent);
          nameEntryStream.close();

          String nameString = new String(nameContent, StandardCharsets.UTF_8);
          nameString = nameString.replace("\0", "").trim();

          String scoreString = new String(scoreContent, StandardCharsets.UTF_8);
          scoreString = scoreString.replace("\0", "");

          VPRegScoreEntry score = new VPRegScoreEntry();
          score.setBase64Score(new Base64Encoder().encode(scoreContent));
          score.setBase64Name(new Base64Encoder().encode(nameContent));
          score.setInitials(nameString);
          score.setScore(StringUtils.isEmpty(scoreString) ?  0 : Long.parseLong(scoreString));
          score.setPos(index);
          summary.getScores().add(score);
          index++;
        }

        return summary;
      }
    } catch (IOException e) {
      LOG.error("Failed to read VPReg.stg: " + e.getMessage(), e);
    } finally {
      if (fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
    return null;
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
