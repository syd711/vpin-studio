package de.mephisto.vpin.server.doftester;

import de.mephisto.vpin.restclient.doftester.ToySummary;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.dof.DOFService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.vpx.VPXUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DOFTesterService {
  private static final Logger LOG = LoggerFactory.getLogger(DOFTesterService.class);

  private static final String SCRIPT_NAME = "dof-test.ps1";

  private static final String PS_SCRIPT =
      "param([string]$DllPath, [string]$ConfigFile, [string]$RomName, [string]$Type, [int]$Number, [int]$DurationMs = 200)\n" +
          "Add-Type -Path $DllPath\n" +
          "$dof = New-Object DirectOutputCom.ComObject\n" +
          "$dof.Init('VPinStudio', $ConfigFile, $RomName)\n" +
          "$dof.UpdateTableElement($Type, $Number, 255)\n" +
          "Start-Sleep -Milliseconds $DurationMs\n" +
          "$dof.UpdateTableElement($Type, $Number, 0)\n" +
          "$dof.Finish()\n";

  @Autowired
  private DOFService dofService;

  @Autowired
  private GameService gameService;

  private static final Pattern DOF_CALL_PATTERN = Pattern.compile("\\bDOF\\s+(\\d+)\\s*,", Pattern.CASE_INSENSITIVE);
  private static final Pattern UPDATE_TABLE_ELEMENT_PATTERN = Pattern.compile("UpdateTableElement\\s+\"([A-Za-z])\"\\s*,\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern RAW_EVENT_CODE_PATTERN = Pattern.compile("^[A-Z]\\d+$");

  private DOFTesterIniParser cachedParser;
  private File cachedIniFile;

  public ToySummary getToys(int gameId) {
    ToySummary result = new ToySummary();
    Game game = gameService.getGame(gameId);
    String romName = (game != null) ? game.getRom() : null;
    if (StringUtils.isEmpty(romName)) {
      result.setToys(scanToys(game, null));
      return result;
    }
    DOFTesterIniParser parser = getParser();
    if (parser == null) {
      return result;
    }

    List<String> toys = parser.getToys(romName);
    result.setDofMapped(!toys.isEmpty());
    if (toys.isEmpty()) {
      toys = scanToys(game, romName);
    }
    result.setToys(toys);
    return result;
  }

  private List<String> scanToys(Game game, String romName) {
    if (game == null) {
      return Collections.emptyList();
    }
    File gameFile = game.getGameFile();
    if (gameFile == null || !gameFile.exists()) {
      return Collections.emptyList();
    }
    String script = VPXUtil.readScript(gameFile);
    Set<DOFEventCode> eventCodes = new LinkedHashSet<>();
    for (String rawLine : script.split("\n")) {
      String line = stripVbsComment(rawLine);
      Matcher dofMatcher = DOF_CALL_PATTERN.matcher(line);
      while (dofMatcher.find()) {
        eventCodes.add(new DOFEventCode("E", Integer.parseInt(dofMatcher.group(1))));
      }
      Matcher updateMatcher = UPDATE_TABLE_ELEMENT_PATTERN.matcher(line);
      while (updateMatcher.find()) {
        eventCodes.add(new DOFEventCode(updateMatcher.group(1).toUpperCase(), Integer.parseInt(updateMatcher.group(2))));
      }
    }
    LOG.info("Scanned VPX script for {}: found {} DOF event codes", gameFile.getName(), eventCodes.size());

    DOFTesterIniParser parser = (romName != null) ? getParser() : null;
    Set<String> seen = new LinkedHashSet<>();
    List<String> result = new ArrayList<>();
    List<DOFEventCode> sorted = new ArrayList<>(eventCodes);
    sorted.sort(Comparator.comparingInt(DOFEventCode::getNumber));
    for (DOFEventCode code : sorted) {
      String toyName = (parser != null) ? parser.getToyNameForCode(romName, code) : null;
      String label = (toyName != null) ? toyName : code.toString();
      if (seen.add(label)) {
        result.add(label);
      }
    }
    return result;
  }

  private static String stripVbsComment(String line) {
    boolean inString = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '"') {
        inString = !inString;
      }
      else if (c == '\'' && !inString) {
        return line.substring(0, i);
      }
    }
    return line;
  }

  public boolean testToy(int gameId, String toyName, int durationMs) {
    File dllFile = new File(dofService.getInstallationFolder(), "DirectOutputComObject.dll");
    if (!dllFile.exists()) {
      LOG.error("DirectOutputComObject.dll not found at {}", dllFile.getAbsolutePath());
      return false;
    }

    // Raw event code path (e.g., "E101", "S24") — used for unmapped / script-driven tables
    if (RAW_EVENT_CODE_PATTERN.matcher(toyName).matches()) {
      DOFEventCode code = new DOFEventCode(String.valueOf(Character.toUpperCase(toyName.charAt(0))), Integer.parseInt(toyName.substring(1)));
      String romName = StringUtils.defaultString(resolveRom(gameId), "");
      File iniFile = getIniFile();
      if (iniFile == null) {
        return false;
      }
      File scriptFile = ensureScript(iniFile.getParentFile());
      return scriptFile != null && runScript(scriptFile, dllFile, iniFile, romName, code, durationMs);
    }

    // Named toy path — ROM must be in the DOF config
    String romName = resolveRom(gameId);
    if (romName == null) {
      LOG.warn("No ROM name found for gameId {}", gameId);
      return false;
    }
    DOFTesterIniParser parser = getParser();
    if (parser == null) {
      LOG.warn("Could not load DOF ini file");
      return false;
    }
    List<DOFEventCode> codes = parser.getEventCodes(romName, toyName);
    if (codes.isEmpty()) {
      LOG.warn("No event codes found for ROM '{}', toy '{}'", romName, toyName);
      return false;
    }
    File iniFile = cachedIniFile;
    File scriptFile = ensureScript(iniFile.getParentFile());
    if (scriptFile == null) {
      return false;
    }
    boolean success = true;
    for (DOFEventCode code : codes) {
      success &= runScript(scriptFile, dllFile, iniFile, romName, code, durationMs);
    }
    return success;
  }

  private boolean runScript(File scriptFile, File dllFile, File iniFile, String romName, DOFEventCode code, int durationMs) {
    try {
      List<String> cmd = Arrays.asList(
          "powershell", "-ExecutionPolicy", "Bypass", "-File", scriptFile.getAbsolutePath(),
          "-DllPath", dllFile.getAbsolutePath(),
          "-ConfigFile", iniFile.getAbsolutePath(),
          "-RomName", romName,
          "-Type", code.getType(),
          "-Number", String.valueOf(code.getNumber()),
          "-DurationMs", String.valueOf(durationMs)
      );
      LOG.info("DOF test: firing {} for ROM '{}', toy event {}", code, romName, code);
      SystemCommandExecutor executor = new SystemCommandExecutor(cmd, false);
      executor.executeCommand();
      String err = executor.getStandardErrorFromCommand().toString().trim();
      if (!StringUtils.isEmpty(err)) {
        LOG.warn("DOF test script stderr: {}", err);
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to run DOF test script for {}: {}", code, e.getMessage(), e);
      return false;
    }
  }

  private String resolveRom(int gameId) {
    Game game = gameService.getGame(gameId);
    if (game == null) {
      return null;
    }
    String rom = game.getRom();
    return StringUtils.isEmpty(rom) ? null : rom;
  }

  private File getIniFile() {
    getParser();
    return cachedIniFile;
  }

  private synchronized DOFTesterIniParser getParser() {
    if (cachedParser != null) {
      return cachedParser;
    }
    File iniFile = findIniFile();
    if (iniFile == null) {
      return null;
    }
    try {
      cachedParser = new DOFTesterIniParser(iniFile);
      cachedIniFile = iniFile;
      return cachedParser;
    }
    catch (IOException e) {
      LOG.error("Failed to parse DOF ini file {}: {}", iniFile.getAbsolutePath(), e.getMessage(), e);
      return null;
    }
  }

  private File findIniFile() {
    File installFolder = dofService.getInstallationFolder();
    if (installFolder == null || !installFolder.exists()) {
      LOG.warn("DOF installation folder not configured or missing");
      return null;
    }
    File configFolder = new File(installFolder, "Config");
    if (!configFolder.exists()) {
      LOG.warn("DOF config folder not found: {}", configFolder.getAbsolutePath());
      return null;
    }
    File[] iniFiles = configFolder.listFiles((dir, name) ->
        name.toLowerCase().startsWith("directoutputconfig") && name.toLowerCase().endsWith(".ini"));
    if (iniFiles == null || iniFiles.length == 0) {
      LOG.warn("No directoutputconfig*.ini found in {}", configFolder.getAbsolutePath());
      return null;
    }
    Arrays.sort(iniFiles);
    return iniFiles[0];
  }

  private File ensureScript(File configFolder) {
    File scriptFile = new File(configFolder, SCRIPT_NAME);
    if (!scriptFile.exists()) {
      try {
        Files.write(scriptFile.toPath(), PS_SCRIPT.getBytes(StandardCharsets.UTF_8));
        LOG.info("Written DOF test script to {}", scriptFile.getAbsolutePath());
      }
      catch (IOException e) {
        LOG.error("Failed to write DOF test script: {}", e.getMessage(), e);
        return null;
      }
    }
    return scriptFile;
  }

  public void invalidateCache() {
    cachedParser = null;
    cachedIniFile = null;
  }
}
