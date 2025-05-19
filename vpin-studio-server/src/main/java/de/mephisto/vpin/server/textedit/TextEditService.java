package de.mephisto.vpin.server.textedit;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.mame.MameRomAliasService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpx.VPXService;
import de.mephisto.vpin.server.vpx.VPXUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class TextEditService {
  private final static Logger LOG = LoggerFactory.getLogger(TextEditService.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private GameService gameService;

  @Autowired
  private MameService mameService;

  @Autowired
  private MameRomAliasService mameRomAliasService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private DOFLinxService dofLinxService;

  @Autowired
  private VPXService vpxService;

  public TextFile getText(TextFile textFile) throws Exception {
    try {
      ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

      VPinFile vPinFile = textFile.getvPinFile();
      switch (vPinFile) {
        case DmdDeviceIni: {
          File mameFolder = mameService.getMameFolder();
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
        case DOFLinxINI: {
          File init = dofLinxService.getDOFLinxINI();
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
        case VPinballXIni: {
          File init = vpxService.getVPXFile();
          Path filePath = init.toPath();
          String iniText = Files.readString(filePath);
          textFile.setContent(iniText);
          textFile.setPath(init.getAbsolutePath());
          textFile.setSize(init.length());
          textFile.setLastModified(new Date(init.lastModified()));
          break;
        }
        case VPMAliasTxt: {
          GameEmulator defaultGameEmulator = emulatorService.getGameEmulator(textFile.getEmulatorId());
          return mameRomAliasService.loadAliasFile(defaultGameEmulator);
        }
        case VBScript: {
          Game game = frontendService.getOriginalGame(textFile.getFileId());
          File gameFile = game.getGameFile();
          String vbs = VPXUtil.exportVBS(gameFile, serverSettings.isKeepVbsFiles());
          textFile.setLastModified(new Date(gameFile.lastModified()));
          textFile.setPath(gameFile.getAbsolutePath());
          textFile.setSize(vbs.getBytes().length);
          textFile.setContent(vbs);
          return textFile;
        }
        case LOCAL: {
          textFile.setLastModified(new Date());
          File f = new File(textFile.getPath());
          if (!f.exists()) {
            throw new UnsupportedOperationException("No such file: " + f.getAbsolutePath());
          }
          String iniText = Files.readString(f.toPath());
          textFile.setContent(iniText);
          textFile.setPath(f.getAbsolutePath());
          textFile.setSize(f.length());
          textFile.setLastModified(new Date(f.lastModified()));
          return textFile;
        }
        default: {
          throw new UnsupportedOperationException("Unknown VPin file: " + vPinFile);
        }
      }

    }
    catch (Exception e) {
      LOG.error("Error reading text file: " + e.getMessage(), e);
      throw e;
    }
    return textFile;
  }

  public TextFile save(TextFile textFile) {
    try {
      ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
      textFile.setLastModified(new Date());
      VPinFile vPinFile = textFile.getvPinFile();
      switch (vPinFile) {
        case DmdDeviceIni: {
          File mameFolder = mameService.getMameFolder();
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
          textFile.setSize(iniFile.length());
          textFile.setLastModified(new Date(iniFile.lastModified()));
          return textFile;
        }
        case DOFLinxINI: {
          File dofLinxIni = dofLinxService.getDOFLinxINI();
          File backup = new File(dofLinxIni.getParentFile(), "DOFLinx.INI.bak");
          if (!backup.exists()) {
            FileUtils.copyFile(dofLinxIni, backup);
          }

          if (dofLinxIni.delete()) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dofLinxIni), StandardCharsets.UTF_8));
            out.write('\ufeff');
            out.write(textFile.getContent());
            out.close();
            LOG.info("Written " + dofLinxIni.getAbsolutePath());
          }
          else {
            throw new IOException("Failed to delete target file.");
          }
          textFile.setLastModified(new Date(dofLinxIni.lastModified()));
          textFile.setSize(dofLinxIni.length());
          return textFile;
        }
        case VPMAliasTxt: {
          GameEmulator defaultGameEmulator = emulatorService.getGameEmulator(textFile.getEmulatorId());
          String[] lines = textFile.getContent().split("\n");
          List<String> sorted = Arrays.asList(lines);
          sorted.sort(Comparator.comparing(String::toLowerCase));
          String content = String.join("\n", sorted);

          mameRomAliasService.saveAliasFile(defaultGameEmulator, content);
          return mameRomAliasService.loadAliasFile(defaultGameEmulator);
        }
        case VBScript: {
          Game game = frontendService.getOriginalGame(textFile.getFileId());
          if (game != null) {
            File gameFile = game.getGameFile();
            VPXUtil.importVBS(gameFile, textFile.getContent(), serverSettings.isKeepVbsFiles());
            textFile.setLastModified(new Date(gameFile.lastModified()));
            textFile.setSize(textFile.getContent().getBytes().length);
            LOG.info("Saved " + gameFile.getAbsolutePath() + ", performing table table.");
            gameService.scanGame(game.getId());
            return textFile;
          }
          else {
            LOG.error("No game found with game name '" + textFile.getFileId() + "'");
          }
        }
        case LOCAL: {
          File f = new File(textFile.getPath());
          String[] lines = textFile.getContent().split("\n");
          List<String> allLines = Arrays.asList(lines);
          String content = String.join("\n", allLines);
          FileUtils.writeStringToFile(f, content, Charset.defaultCharset());
          LOG.info("Written " + f.getAbsolutePath());
          textFile.setLastModified(new Date(f.lastModified()));
          textFile.setSize(textFile.getContent().getBytes().length);
          return textFile;
        }
        case VPinballXIni: {
          File f = vpxService.getVPXFile();
          String[] lines = textFile.getContent().split("\n");
          List<String> allLines = Arrays.asList(lines);
          String content = String.join("\n", allLines);
          FileUtils.writeStringToFile(f, content, Charset.defaultCharset());
          LOG.info("Written " + f.getAbsolutePath());
          textFile.setLastModified(new Date(f.lastModified()));
          textFile.setSize(textFile.getContent().getBytes().length);
          return textFile;
        }
        default: {
          throw new UnsupportedOperationException("Unknown VPin file: " + vPinFile);
        }
      }
    }
    catch (IOException e) {
      LOG.error("Error reading text file: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
