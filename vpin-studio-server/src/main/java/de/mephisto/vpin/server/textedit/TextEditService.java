package de.mephisto.vpin.server.textedit;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
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

  public MonitoredTextFile getText(MonitoredTextFile monitoredTextFile) throws Exception {
    try {
      ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

      VPinFile vPinFile = monitoredTextFile.getvPinFile();
      switch (vPinFile) {
        case DmdDeviceIni: {
          File mameFolder = mameService.getMameFolder();
          File init = new File(mameFolder, "DmdDevice.ini");
          Path filePath = init.toPath();
          String iniText = Files.readString(filePath);
          //Remove BOM
          iniText = iniText.replace("\uFEFF", "");
          monitoredTextFile.setContent(iniText);
          monitoredTextFile.setPath(init.getAbsolutePath());
          monitoredTextFile.setSize(init.length());
          monitoredTextFile.setLastModified(new Date(init.lastModified()));
          break;
        }
        case DOFLinxINI: {
          File init = dofLinxService.getDOFLinxINI();
          Path filePath = init.toPath();
          String iniText = Files.readString(filePath);
          //Remove BOM
          iniText = iniText.replace("\uFEFF", "");
          monitoredTextFile.setContent(iniText);
          monitoredTextFile.setPath(init.getAbsolutePath());
          monitoredTextFile.setSize(init.length());
          monitoredTextFile.setLastModified(new Date(init.lastModified()));
          break;
        }
        case VPinballXIni: {
          File init = vpxService.getVPXFile();
          Path filePath = init.toPath();
          String iniText = Files.readString(filePath);
          monitoredTextFile.setContent(iniText);
          monitoredTextFile.setPath(init.getAbsolutePath());
          monitoredTextFile.setSize(init.length());
          monitoredTextFile.setLastModified(new Date(init.lastModified()));
          break;
        }
        case VPMAliasTxt: {
          GameEmulator defaultGameEmulator = emulatorService.getGameEmulator(monitoredTextFile.getEmulatorId());
          return mameRomAliasService.loadAliasFile(defaultGameEmulator);
        }
        case VBScript: {
          Game game = frontendService.getOriginalGame(Integer.parseInt(monitoredTextFile.getFileId()));
          File gameFile = game.getGameFile();
          String vbs = VPXUtil.exportVBS(gameFile, serverSettings.isKeepVbsFiles());
          monitoredTextFile.setLastModified(new Date(gameFile.lastModified()));
          monitoredTextFile.setPath(gameFile.getAbsolutePath());
          monitoredTextFile.setSize(vbs.getBytes().length);
          monitoredTextFile.setContent(vbs);
          return monitoredTextFile;
        }
        case LOCAL_GAME_FILE: {
          monitoredTextFile.setLastModified(new Date());
          File f = new File(monitoredTextFile.getPath());
          if (!f.exists()) {
            throw new UnsupportedOperationException("No such file: " + f.getAbsolutePath());
          }
          String iniText = Files.readString(f.toPath());
          monitoredTextFile.setContent(iniText);
          monitoredTextFile.setPath(f.getAbsolutePath());
          monitoredTextFile.setSize(f.length());
          monitoredTextFile.setLastModified(new Date(f.lastModified()));
          return monitoredTextFile;
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
    return monitoredTextFile;
  }

  public MonitoredTextFile save(MonitoredTextFile monitoredTextFile) {
    try {
      ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
      monitoredTextFile.setLastModified(new Date());
      VPinFile vPinFile = monitoredTextFile.getvPinFile();
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
            out.write(monitoredTextFile.getContent());
            out.close();
            LOG.info("Written " + iniFile.getAbsolutePath());
          }
          else {
            throw new IOException("Failed to delete target file.");
          }
          monitoredTextFile.setSize(iniFile.length());
          monitoredTextFile.setLastModified(new Date(iniFile.lastModified()));
          return monitoredTextFile;
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
            out.write(monitoredTextFile.getContent());
            out.close();
            LOG.info("Written " + dofLinxIni.getAbsolutePath());
          }
          else {
            throw new IOException("Failed to delete target file.");
          }
          monitoredTextFile.setLastModified(new Date(dofLinxIni.lastModified()));
          monitoredTextFile.setSize(dofLinxIni.length());
          return monitoredTextFile;
        }
        case VPMAliasTxt: {
          GameEmulator defaultGameEmulator = emulatorService.getGameEmulator(monitoredTextFile.getEmulatorId());
          String[] lines = monitoredTextFile.getContent().split("\n");
          List<String> sorted = Arrays.asList(lines);
          sorted.sort(Comparator.comparing(String::toLowerCase));
          String content = String.join("\n", sorted);

          mameRomAliasService.saveAliasFile(defaultGameEmulator, content);
          return mameRomAliasService.loadAliasFile(defaultGameEmulator);
        }
        case VBScript: {
          Game game = frontendService.getOriginalGame(Integer.parseInt(monitoredTextFile.getFileId()));
          if (game != null) {
            File gameFile = game.getGameFile();
            VPXUtil.importVBS(gameFile, monitoredTextFile.getContent(), serverSettings.isKeepVbsFiles());
            monitoredTextFile.setLastModified(new Date(gameFile.lastModified()));
            monitoredTextFile.setSize(monitoredTextFile.getContent().getBytes().length);
            LOG.info("Saved " + gameFile.getAbsolutePath() + ", performing table table.");
            gameService.scanGame(game.getId());
            return monitoredTextFile;
          }
          else {
            LOG.error("No game found with game name '" + monitoredTextFile.getFileId() + "'");
          }
        }
        case LOCAL_GAME_FILE: {
          File f = new File(monitoredTextFile.getPath());
          String[] lines = monitoredTextFile.getContent().split("\n");
          List<String> allLines = Arrays.asList(lines);
          String content = String.join("\n", allLines);
          FileUtils.writeStringToFile(f, content, Charset.defaultCharset());
          LOG.info("Written " + f.getAbsolutePath());
          monitoredTextFile.setLastModified(new Date(f.lastModified()));
          monitoredTextFile.setSize(monitoredTextFile.getContent().getBytes().length);
          return monitoredTextFile;
        }
        case VPinballXIni: {
          File f = vpxService.getVPXFile();
          String[] lines = monitoredTextFile.getContent().split("\n");
          List<String> allLines = Arrays.asList(lines);
          String content = String.join("\n", allLines);
          FileUtils.writeStringToFile(f, content, Charset.defaultCharset());
          LOG.info("Written " + f.getAbsolutePath());
          monitoredTextFile.setLastModified(new Date(f.lastModified()));
          monitoredTextFile.setSize(monitoredTextFile.getContent().getBytes().length);
          return monitoredTextFile;
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
