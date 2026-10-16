package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSoundEntry;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundInfo;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundNames;
import de.mephisto.vpin.server.games.Game;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Shares the sound names of an ALT sound pack with the rom2altsound community naming effort.
 * The names are merged into one shared set that feeds the ALT sound packs and the sounds.dat of PinMAME.
 * <p>
 * The names are taken from the audio filenames, e.g. "Martian laugh" from "0x0002-Martian_laugh.ogg".
 */
@Service
public class Rom2AltSoundNamesService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Anonymous, write-only drop, missing folders are created and existing files are never overwritten.
   */
  private static final String UPLOAD_URL = "https://nextcloud.syl21.org/public.php/dav/files/MLWYGCsA3gN8AoY/sound_dat/";
  private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
  private static final String MANIFEST_FILE = "manifest.json";

  private static final Pattern ID_PREFIX_PATTERN = Pattern.compile("^0x[0-9a-fA-F]+[-_ ]*");
  // twins of older packs, e.g. "sameas-000148-success_in_mission"
  private static final Pattern SAME_AS_PATTERN = Pattern.compile("^sameas-\\d+-", Pattern.CASE_INSENSITIVE);
  // the loop and intro files of rom2altsound, e.g. "0x0003-afm_113b-loop.wav"
  private static final Pattern LOOP_SUFFIX_PATTERN = Pattern.compile("[-_ ](loop|extended)$", Pattern.CASE_INSENSITIVE);

  private final ObjectMapper objectMapper = JsonMapper.builder().build();

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private Rom2AltSoundService rom2AltSoundService;

  /**
   * The names that would be shared for the ALT sound of the game, the error is set when they cannot be shared.
   */
  @NonNull
  public Rom2AltSoundNames getNames(@NonNull Game game) {
    Rom2AltSoundNames names = new Rom2AltSoundNames();
    File altSoundFolder = altSoundService.getAltSoundFolder(game);
    if (altSoundFolder == null || !altSoundFolder.exists()) {
      names.setError("No ALT sound folder found for \"" + game.getGameDisplayName() + "\".");
      return names;
    }

    readManifest(names, new File(altSoundFolder, MANIFEST_FILE));
    if (names.getRom() == null) {
      names.setRom(altSoundFolder.getName());
    }
    if (names.getSoundRomId() == null && rom2AltSoundService.isAvailable()) {
      // packs not made by rom2altsound: the sound ROM is identified by the ROM check
      Rom2AltSoundInfo info = rom2AltSoundService.getInfo(game);
      names.setSoundRomId(info.getSoundRomId());
    }
    if (names.getVersion() == null) {
      names.setVersion(rom2AltSoundService.getInstalledVersion());
    }
    if (names.getSoundRomId() == null) {
      names.setError("The sound ROM of \"" + names.getRom() + "\" could not be identified, "
          + "the pack has no " + MANIFEST_FILE + " and rom2altsound is not installed.");
      return names;
    }

    AltSound altSound = altSoundService.getAltSound(game);
    Map<String, String> byId = new LinkedHashMap<>();
    for (AltSoundEntry entry : altSound.getEntries()) {
      String name = toName(entry, names.getRom());
      if (name != null && !StringUtils.isEmpty(entry.getId())) {
        byId.putIfAbsent(entry.getId(), name);
      }
    }
    byId.forEach((id, name) -> names.getEntries().add(new Rom2AltSoundNames.Entry(id, name)));

    if (names.getEntries().isEmpty()) {
      names.setError("No named sounds found, rename the audio files to name their sounds.");
    }
    return names;
  }

  /**
   * Uploads the names of the ALT sound of the game, returns them with the name of the uploaded file.
   */
  @NonNull
  public Rom2AltSoundNames upload(@NonNull Game game) throws Exception {
    Rom2AltSoundNames names = getNames(game);
    if (!names.isExportable()) {
      throw new IllegalStateException(names.getError());
    }

    String filename = ZonedDateTime.now(ZoneOffset.UTC).format(TIMESTAMP_FORMAT) + ".csv";
    String url = UPLOAD_URL + encode(names.getRom()) + "/" + encode(names.getSoundRomId()) + "/" + filename;
    String csv = toCSV(names);

    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .build();
    HttpRequest request = HttpRequest.newBuilder(URI.create(url))
        .header("User-Agent", "VPin-Studio")
        .header("Content-Type", "text/csv; charset=utf-8")
        .timeout(Duration.ofSeconds(60))
        .PUT(HttpRequest.BodyPublishers.ofString(csv, StandardCharsets.UTF_8))
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IllegalStateException("The upload failed with HTTP " + response.statusCode());
    }

    LOG.info("Uploaded {} sound names of \"{}\" (sound ROM {}) as {}", names.getEntries().size(), names.getRom(), names.getSoundRomId(), filename);
    names.setUploadedFile(filename);
    return names;
  }

  /**
   * The names.csv of rom2altsound: a short header, then the ID and NAME columns, CRLF and RFC 4180 quoting.
   */
  @NonNull
  static String toCSV(@NonNull Rom2AltSoundNames names) throws Exception {
    StringBuilder out = new StringBuilder();
    try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.RFC4180)) {
      out.append("rom2altsound names\r\n");
      out.append("rom: ").append(names.getRom()).append("\r\n");
      out.append("sound_rom_id: ").append(names.getSoundRomId()).append("\r\n");
      out.append("rom2altsound: ").append(StringUtils.defaultString(names.getVersion())).append("\r\n");
      printer.printRecord("ID", "NAME");
      for (Rom2AltSoundNames.Entry entry : names.getEntries()) {
        printer.printRecord(entry.getId(), entry.getName());
      }
    }
    return out.toString();
  }

  /**
   * The name of the sound from its audio filename, null when the file still has its generic, id-based name.
   */
  @Nullable
  static String toName(@NonNull AltSoundEntry entry, @Nullable String rom) {
    String baseName = FilenameUtils.getBaseName(StringUtils.defaultString(entry.getFilename()).replace('\\', '/'));

    String id = entry.getId();
    if (!StringUtils.isEmpty(id) && StringUtils.startsWithIgnoreCase(baseName, id)) {
      baseName = StringUtils.stripStart(baseName.substring(id.length()), "-_ ");
    }
    else {
      baseName = ID_PREFIX_PATTERN.matcher(baseName).replaceFirst("");
    }
    baseName = SAME_AS_PATTERN.matcher(baseName).replaceFirst("");
    baseName = LOOP_SUFFIX_PATTERN.matcher(baseName).replaceFirst("");

    String name = StringUtils.normalizeSpace(baseName.replace('_', ' '));
    if (name.isEmpty()) {
      return null;
    }
    // rom2altsound names the files after the ROM, e.g. "0x0001-afm_113b.wav"
    if (rom != null && StringUtils.normalizeSpace(rom.replace('_', ' ')).equalsIgnoreCase(name)) {
      return null;
    }
    return name;
  }

  private void readManifest(@NonNull Rom2AltSoundNames names, @NonNull File manifest) {
    if (!manifest.isFile()) {
      return;
    }
    try {
      JsonNode root = objectMapper.readTree(manifest);
      names.setRom(findText(root, "rom"));
      names.setSoundRomId(findText(root, "sound_rom_id"));
      String version = findText(root, "rom2altsound");
      names.setVersion(version != null ? version : findText(root, "version"));
    }
    catch (Exception e) {
      LOG.warn("Failed to read {}: {}", manifest.getAbsolutePath(), e.getMessage());
    }
  }

  /**
   * The value of the field at the top level, or the first one found below it.
   */
  @Nullable
  private static String findText(@NonNull JsonNode root, @NonNull String field) {
    JsonNode value = root.path(field);
    if (value.isMissingNode() || value.isNull()) {
      value = root.findValue(field);
    }
    if (value == null || !value.isValueNode() || value.isNull()) {
      return null;
    }
    return StringUtils.trimToNull(value.asString());
  }

  @NonNull
  private static String encode(@NonNull String segment) {
    return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
  }
}
