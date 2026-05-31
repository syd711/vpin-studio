package de.mephisto.vpin.server.textedit;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.textedit.TextEditorFile;
import de.mephisto.vpin.restclient.textedit.TextEditorFileTypes;
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpinmame.VPinMameRomAliasService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
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
  private VPinMameService vPinMameService;

  @Autowired
  private VPinMameRomAliasService VPinMameRomAliasService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private DOFLinxService dofLinxService;

  @Autowired
  private VPXService vpxService;

  public TextEditorFile getText(TextEditorFile textEditorFile) throws Exception {
    try {
      ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

      TextEditorFileTypes textEditorFileTypes = textEditorFile.getFile() != null
                ? textEditorFile.getFile()
                : TextEditorFileTypes.valueOf(textEditorFile.getContent());

      switch (textEditorFileTypes) {
        case DmdDeviceIni: {
          File mameFolder = vPinMameService.getMameFolder();
          File init = new File(mameFolder, "DmdDevice.ini");
          Path filePath = init.toPath();
          String iniText = Files.readString(filePath);
          //Remove BOM
          iniText = iniText.replace("\uFEFF", "");
          textEditorFile.setContent(iniText);
          textEditorFile.setPath(init.getAbsolutePath());
          textEditorFile.setSize(init.length());
          textEditorFile.setLastModified(OffsetDateTime.ofInstant(Instant.ofEpochMilli(init.lastModified()), ZoneId.systemDefault()));
            
          break;
        }
        case DOFLinxINI: {
          File init = dofLinxService.getDOFLinxINI();
          Path filePath = init.toPath();
          String iniText = Files.readString(filePath);
          //Remove BOM
          iniText = iniText.replace("\uFEFF", "");
          textEditorFile.setContent(iniText);
          textEditorFile.setPath(init.getAbsolutePath());
          textEditorFile.setSize(init.length());
          textEditorFile.setLastModified(OffsetDateTime.ofInstant(Instant.ofEpochMilli(init.lastModified()), ZoneId.systemDefault()));
          break;
        }
        case VPinballXIni: {
          File init = vpxService.getVPXFile();
          Path filePath = init.toPath();
          String iniText = Files.readString(filePath);
          textEditorFile.setContent(iniText);
          textEditorFile.setPath(init.getAbsolutePath());
          textEditorFile.setSize(init.length());
          textEditorFile.setLastModified(OffsetDateTime.ofInstant(Instant.ofEpochMilli(init.lastModified()), ZoneId.systemDefault()));
          break;
        }
        case VPMAliasTxt: {
          GameEmulator defaultGameEmulator = emulatorService.getGameEmulator(textEditorFile.getEmulatorId());
          return VPinMameRomAliasService.loadAliasFile(defaultGameEmulator);
        }
        case VBScript: {
          Game game = frontendService.getOriginalGame(Integer.parseInt(textEditorFile.getFileId()));
          File gameFile = game.getGameFile();
          String vbs = VPXUtil.exportVBS(gameFile, serverSettings.isKeepVbsFiles());
          textEditorFile.setLastModified(OffsetDateTime.ofInstant(Instant.ofEpochMilli(gameFile.lastModified()), ZoneId.systemDefault()));
          textEditorFile.setPath(gameFile.getAbsolutePath());
          textEditorFile.setSize(vbs.getBytes().length);
          textEditorFile.setContent(vbs);
          return textEditorFile;
        }
        case LOCAL_GAME_FILE: {
          textEditorFile.setLastModified(OffsetDateTime.now());
          File f = new File(textEditorFile.getPath());
          if (!f.exists()) {
            throw new UnsupportedOperationException("No such file: " + f.getAbsolutePath());
          }
          String iniText = Files.readString(f.toPath());
          textEditorFile.setContent(iniText);
          textEditorFile.setPath(f.getAbsolutePath());
          textEditorFile.setSize(f.length());
          textEditorFile.setLastModified(OffsetDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()), ZoneId.systemDefault()));
          return textEditorFile;
        }
        default: {
          throw new UnsupportedOperationException("Unknown VPin file: " + textEditorFileTypes);
        }
      }

    }
    catch (Exception e) {
      LOG.error("Error reading text file: {}", e.getMessage(), e);
      throw e;
    }
    return textEditorFile;
  }

  public TextEditorFile save(TextEditorFile textEditorFile) {
    try {
      ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
      textEditorFile.setLastModified(OffsetDateTime.now());

        TextEditorFileTypes textEditorFileTypes = textEditorFile.getFile() != null
                ? textEditorFile.getFile()
                : TextEditorFileTypes.valueOf(textEditorFile.getContent());

      switch (textEditorFileTypes) {
        case DmdDeviceIni: {
          File mameFolder = vPinMameService.getMameFolder();
          File iniFile = new File(mameFolder, "DmdDevice.ini");
          File backup = new File(mameFolder, "DmdDevice.ini.bak");
          if (!backup.exists()) {
            FileUtils.copyFile(iniFile, backup);
          }

          if (iniFile.delete()) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(iniFile), StandardCharsets.UTF_8));
            out.write('\ufeff');
            out.write(textEditorFile.getContent());
            out.close();
            LOG.info("Written {}", iniFile.getAbsolutePath());
          }
          else {
            throw new IOException("Failed to delete target file.");
          }
          textEditorFile.setSize(iniFile.length());
          textEditorFile.setLastModified(OffsetDateTime.ofInstant(Instant.ofEpochMilli(iniFile.lastModified()), ZoneId.systemDefault()));
          return textEditorFile;
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
            out.write(textEditorFile.getContent());
            out.close();
            LOG.info("Written {}", dofLinxIni.getAbsolutePath());
          }
          else {
            throw new IOException("Failed to delete target file.");
          }
          textEditorFile.setLastModified(OffsetDateTime.ofInstant(Instant.ofEpochMilli(dofLinxIni.lastModified()), ZoneId.systemDefault()));
          textEditorFile.setSize(dofLinxIni.length());
          return textEditorFile;
        }
        case VPMAliasTxt: {
          GameEmulator defaultGameEmulator = emulatorService.getGameEmulator(textEditorFile.getEmulatorId());
          String[] lines = textEditorFile.getContent().split("\n");
          List<String> sorted = Arrays.asList(lines);
          sorted.sort(Comparator.comparing(String::toLowerCase));
          String content = String.join("\n", sorted);

          VPinMameRomAliasService.saveAliasFile(defaultGameEmulator, content);
          return VPinMameRomAliasService.loadAliasFile(defaultGameEmulator);
        }
        case VBScript: {
          Game game = frontendService.getOriginalGame(Integer.parseInt(textEditorFile.getFileId()));
          if (game != null) {
            File gameFile = game.getGameFile();
            VPXUtil.importVBS(gameFile, textEditorFile.getContent(), serverSettings.isKeepVbsFiles());
            textEditorFile.setLastModified(OffsetDateTime.ofInstant(Instant.ofEpochMilli(gameFile.lastModified()), ZoneId.systemDefault()));
            textEditorFile.setSize(textEditorFile.getContent().getBytes().length);
            LOG.info("Saved {}, performing table table.", gameFile.getAbsolutePath());
            gameService.scanGame(game.getId(), true);
            return textEditorFile;
          }
          else {
            LOG.error("No game found with game name '{}'", textEditorFile.getFileId());
          }
        }
        case LOCAL_GAME_FILE: {
          File f = new File(textEditorFile.getPath());
          String[] lines = textEditorFile.getContent().split("\n");
          List<String> allLines = Arrays.asList(lines);
          String content = String.join("\n", allLines);
          FileUtils.writeStringToFile(f, content, Charset.defaultCharset());
          LOG.info("Written {}", f.getAbsolutePath());
          textEditorFile.setLastModified(OffsetDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()), ZoneId.systemDefault()));
          textEditorFile.setSize(textEditorFile.getContent().getBytes().length);
          return textEditorFile;
        }
        case VPinballXIni: {
          File f = vpxService.getVPXFile();
          String[] lines = textEditorFile.getContent().split("\n");
          List<String> allLines = Arrays.asList(lines);
          String content = String.join("\n", allLines);
          FileUtils.writeStringToFile(f, content, Charset.defaultCharset());
          LOG.info("Written {}", f.getAbsolutePath());
          textEditorFile.setLastModified(OffsetDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()), ZoneId.systemDefault()));
          textEditorFile.setSize(textEditorFile.getContent().getBytes().length);
          return textEditorFile;
        }
        default: {
          throw new UnsupportedOperationException("Unknown VPin file: " + textEditorFileTypes);
        }
      }
    }
    catch (IOException e) {
      LOG.error("Error reading text file: {}", e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
