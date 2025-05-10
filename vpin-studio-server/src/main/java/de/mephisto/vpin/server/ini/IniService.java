package de.mephisto.vpin.server.ini;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.ini.IniRepresentation;
import de.mephisto.vpin.restclient.ini.IniSectionRepresentation;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class IniService {
  private final static Logger LOG = LoggerFactory.getLogger(IniService.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  public boolean delete(int gameId) {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      File iniFile = game.getIniFile();
      if (iniFile.exists() && iniFile.delete()) {
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.INI, null);
        return true;
      }
    }
    return false;
  }

  public IniRepresentation getIniFile(int gameId) throws Exception {
    IniRepresentation iniRepresentation = new IniRepresentation();
    Game game = gameService.getGame(gameId);
    File iniFile = game.getIniFile();
    if (iniFile.exists()) {
      iniRepresentation.setFileName(iniFile.getName());

      INIConfiguration iniConfiguration = readIniConfiguration(iniFile);
      Set<String> sections = iniConfiguration.getSections();
      for (String section : sections) {
        SubnodeConfiguration s = iniConfiguration.getSection(section);
        IniSectionRepresentation sectionRepresentation = new IniSectionRepresentation();
        sectionRepresentation.setName(section);
        Iterator<String> keys = s.getKeys();
        while (keys.hasNext()) {
          String next = keys.next();
          sectionRepresentation.getValues().put(next, s.getProperty(next));
        }
        iniRepresentation.getSections().add(sectionRepresentation);
      }
    }
    return iniRepresentation;
  }

  private INIConfiguration readIniConfiguration(@NonNull File iniFile) throws Exception {
    INIConfiguration iniConfiguration = new INIConfiguration();
    iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    iniConfiguration.setSeparatorUsedInOutput(" = ");
    iniConfiguration.setSeparatorUsedInInput("=");

    FileReader fileReader = new FileReader(iniFile, StandardCharsets.UTF_8);
    try {
      iniConfiguration.read(fileReader);
    }
    catch (Exception e) {
      LOG.error("Failed to read: " + iniFile.getAbsolutePath() + ": " + e.getMessage(), e);
      throw e;
    }
    finally {
      fileReader.close();
    }

    return iniConfiguration;
  }

  public boolean save(int gameId, IniRepresentation iniRepresentation) throws Exception {
    Game game = gameService.getGame(gameId);
    File iniFile = game.getIniFile();
    if (iniFile.exists()) {
      INIConfiguration iniConfiguration = readIniConfiguration(iniFile);

      List<IniSectionRepresentation> sections = iniRepresentation.getSections();
      for (IniSectionRepresentation section : sections) {
        SubnodeConfiguration iniSection = iniConfiguration.getSection(section.getName());
        Map<String, Object> values = section.getValues();
        Set<Map.Entry<String, Object>> entries = values.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
          iniSection.setProperty(entry.getKey(), entry.getValue());
        }
      }

      saveIniFile(iniFile, iniConfiguration);
      gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.INI, null);
      return true;
    }
    return false;
  }

  private void saveIniFile(File iniFile, INIConfiguration iniConfiguration) throws IOException {
    FileWriter fileWriter = new FileWriter(iniFile, StandardCharsets.UTF_8);
    try {
      iniConfiguration.write(fileWriter);
    }
    catch (Exception e) {
      LOG.error("Failed to write ini: " + e.getMessage(), e);
    }
    finally {
      try {
        fileWriter.close();
      }
      catch (IOException e) {
        //ignore
      }
    }
  }
}
