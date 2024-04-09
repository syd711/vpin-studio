package de.mephisto.vpin.server.textedit;

import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.mame.MameRomAliasService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.vpx.VPXUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class TextEditService {
  private final static Logger LOG = LoggerFactory.getLogger(TextEditService.class);

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private GameService gameService;

  @Autowired
  private MameRomAliasService mameRomAliasService;

  public TextFile getText(TextFile textFile) {
    try {
      VPinFile vPinFile = textFile.getvPinFile();
      switch (vPinFile) {
        case DmdDeviceIni: {
          GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
          File mameFolder = defaultGameEmulator.getMameFolder();
          File init = new File(mameFolder, "DmdDevice.ini");
          Path filePath = init.toPath();
          String iniText = Files.readString(filePath);
          //Remove BOM
          iniText = iniText.replace("\uFEFF", "");
          textFile.setContent(iniText);
          textFile.setPath(init.getAbsolutePath());
          textFile.setSize(init.length());
          textFile.setLastModified(new Date(init.lastModified()));
          break;
        }
        case VPMAliasTxt: {
          GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
          return mameRomAliasService.loadAliasFile(defaultGameEmulator);
        }
        case VBScript: {
          Game game = pinUPConnector.getGame(textFile.getFileId());
          File gameFile = game.getGameFile();
          String vbs = VPXUtil.exportVBS(gameFile, textFile.getContent());
          textFile.setLastModified(new Date(gameFile.lastModified()));
          textFile.setPath(gameFile.getAbsolutePath());
          textFile.setSize(vbs.getBytes().length);
          textFile.setContent(vbs);
          return textFile;
        }
        default: {
          throw new UnsupportedOperationException("Unknown VPin file: " + vPinFile);
        }
      }

    } catch (IOException e) {
      LOG.error("Error reading text file: " + e.getMessage(), e);
    }
    return textFile;
  }

  public TextFile save(TextFile textFile) {
    try {
      VPinFile vPinFile = textFile.getvPinFile();
      switch (vPinFile) {
        case DmdDeviceIni: {
          GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
          File mameFolder = defaultGameEmulator.getMameFolder();
          File iniFile = new File(mameFolder, "DmdDevice.ini");
          File backup = new File(mameFolder, "DmdDevice.ini.bak");
          if (!backup.exists()) {
            FileUtils.copyFile(iniFile, backup);
          }

          if (iniFile.delete()) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(iniFile), StandardCharsets.UTF_8));
            out.write('\ufeff');
            out.write(textFile.getContent());
            out.close();
            LOG.info("Written " + iniFile.getAbsolutePath());
          }
          else {
            throw new IOException("Failed to delete target file.");
          }
          return textFile;
        }
        case VPMAliasTxt: {
          GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
          mameRomAliasService.saveAliasFile(defaultGameEmulator, textFile.getContent());
          return mameRomAliasService.loadAliasFile(defaultGameEmulator);
        }
        case VBScript: {
          Game game = pinUPConnector.getGame(textFile.getFileId());
          File gameFile = game.getGameFile();
          VPXUtil.importVBS(gameFile, textFile.getContent());
          textFile.setLastModified(new Date(gameFile.lastModified()));
          textFile.setSize(textFile.getContent().getBytes().length);
          LOG.info("Saved " + gameFile.getAbsolutePath()+ ", performing table table.");
          gameService.scanGame(textFile.getFileId());
          return textFile;
        }
        default: {
          throw new UnsupportedOperationException("Unknown VPin file: " + vPinFile);
        }
      }
    } catch (IOException e) {
      LOG.error("Error reading text file: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
