package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.vpx.TableScriptOption;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VpxScriptOptionsService {

  private static final Logger LOG = LoggerFactory.getLogger(VpxScriptOptionsService.class);

  /* VPX uses TableOption to store data (not TableOptions) */
  static final String INI_SECTION = "TableOption";

    private static final Pattern OPTION_PATTERN = Pattern.compile(
            "^[^'\n]*\\w+\\.Option\\s*\\(\\s*" +
                    "\"([^\"]+)\"\\s*,\\s*" +
                    "([+-]?[\\d]*\\.?[\\d]+(?:[eE][+-]?[\\d]+)?)\\s*,\\s*" +
                    "([+-]?[\\d]*\\.?[\\d]+(?:[eE][+-]?[\\d]+)?)\\s*,\\s*" +
                    "([+-]?[\\d]*\\.?[\\d]+(?:[eE][+-]?[\\d]+)?)\\s*,\\s*" +
                    "([+-]?[\\d]*\\.?[\\d]+(?:[eE][+-]?[\\d]+)?)\\s*,\\s*" +
                    "([01])" +
                    "(?:\\s*,\\s*Array\\s*\\((.*?)\\))?" +
                    "\\s*\\)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

  private static final Pattern ARRAY_STRING_PATTERN = Pattern.compile("\"([^\"]+)\"");

  @Autowired
  private GameService gameService;

  @Autowired
  private VPXService vpxService;

  // ── Public API ────────────────────────────────────────────────────────────

  public List<TableScriptOption> getOptions(int gameId) {
    Game game = gameService.getGame(gameId);
    if (game == null) {
      LOG.warn("getOptions: game {} not found", gameId);
      return new ArrayList<>();
    }

    // Use the existing VPXService.getScript() which handles VBS extraction
    if (game.getGameFile().exists()) {
      String script = vpxService.getScript(game);
      if (script == null || script.isBlank()) {
        LOG.debug("getOptions: no script content for game {}", gameId);
        return new ArrayList<>();
      }

      List<TableScriptOption> options = parseOptions(script);
      populateCurrentValues(options, getIniFile(game));
      return options;
    }

    return Collections.emptyList();
  }

  public boolean saveOptions(int gameId, List<TableScriptOption> options) {
    Game game = gameService.getGame(gameId);
    if (game == null) {
      LOG.warn("saveOptions: game {} not found", gameId);
      return false;
    }

    File iniFile = getIniFile(game);
    try {
      INIConfiguration ini = loadOrCreateIni(iniFile);

 /*Clear the section first*/
        ini.clearTree(INI_SECTION);

/*Sort (VPX writes alphabetically, let's match) */
        List<TableScriptOption> sorted = new ArrayList<>(options);
        sorted.sort(Comparator.comparing(TableScriptOption::getName, String.CASE_INSENSITIVE_ORDER));

        for (TableScriptOption option : sorted) {
            ini.setProperty(INI_SECTION + "." + option.getName(), formatValue(option.getCurrentValue()));
        }

      try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(iniFile), StandardCharsets.UTF_8))) {
        ini.write(writer);
      }

      LOG.info("saveOptions: saved {} options for game {} to {}", options.size(), gameId, iniFile);
      return true;
    }
    catch (Exception e) {
      LOG.error("saveOptions: failed to write INI for game {}: {}", gameId, e.getMessage(), e);
      return false;
    }
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  List<TableScriptOption> parseOptions(String script) {
    List<TableScriptOption> result = new ArrayList<>();
    Matcher m = OPTION_PATTERN.matcher(script);

    while (m.find()) {
      String name = m.group(1).trim();
      if (result.stream().anyMatch(o -> o.getName().equalsIgnoreCase(name))) {
        continue;
      }

      try {
        double min = Double.parseDouble(m.group(2));
        double max = Double.parseDouble(m.group(3));
        double step = Double.parseDouble(m.group(4));
        double def = Double.parseDouble(m.group(5));
        int unit = Integer.parseInt(m.group(6));

        TableScriptOption option = new TableScriptOption(name, min, max, step, def, unit);

        String arrayContent = m.group(7);
        if (arrayContent != null) {
          List<String> labels = new ArrayList<>();
          Matcher am = ARRAY_STRING_PATTERN.matcher(arrayContent);
          while (am.find()) {
            labels.add(am.group(1));
          }
          option.setLiteralOptions(labels);
        }

        result.add(option);
      }
      catch (NumberFormatException e) {
        LOG.warn("parseOptions: could not parse numeric arguments for option '{}': {}", name, e.getMessage());
      }
    }

    result.sort(Comparator.comparing(TableScriptOption::getName));
    LOG.info("parseOptions: found {} options", result.size());
    return result;
  }

  private void populateCurrentValues(List<TableScriptOption> options, File iniFile) {
    if (options.isEmpty()) return;

    INIConfiguration ini = null;
    if (iniFile.exists()) {
      try {
        ini = loadOrCreateIni(iniFile);
      }
      catch (Exception e) {
        LOG.warn("populateCurrentValues: could not read INI {}: {}", iniFile, e.getMessage());
      }
    }

    for (TableScriptOption option : options) {
      double value = option.getDefaultValue();
      if (ini != null) {
        try {
          SubnodeConfiguration section = ini.getSection(INI_SECTION);
          if (section != null && section.containsKey(option.getName())) {
            value = section.getDouble(option.getName(), option.getDefaultValue());
          }
        }
        catch (Exception e) {
          LOG.warn("populateCurrentValues: error reading '{}': {}", option.getName(), e.getMessage());
        }
      }
      option.setCurrentValue(value);
    }
  }

  private File getIniFile(Game game) {
    File vpxFile = game.getGameFile();
    String baseName = vpxFile.getName();
    int dotIdx = baseName.lastIndexOf('.');
    String iniName = (dotIdx > 0 ? baseName.substring(0, dotIdx) : baseName) + ".ini";
    return new File(vpxFile.getParentFile(), iniName);
  }

  private INIConfiguration loadOrCreateIni(File iniFile) throws Exception {
    INIConfiguration ini = new INIConfiguration();
    ini.setListDelimiterHandler(
        org.apache.commons.configuration2.convert.DisabledListDelimiterHandler.INSTANCE);
    if (iniFile.exists()) {
      try (Reader reader = new BufferedReader(
          new InputStreamReader(new FileInputStream(iniFile), StandardCharsets.UTF_8))) {
        ini.read(reader);
      }
    }
    return ini;
  }

    private String formatValue(double value) {
     /* Formatted to match how VPX writes the options */
        return String.format("%.6f", value);
    }
}
