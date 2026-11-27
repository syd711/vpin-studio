package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.mcp.McpToolGroup;
import de.mephisto.vpin.restclient.recorder.RecordingData;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.assets.TableAssetSourcesService;
import de.mephisto.vpin.server.assets.TableAssetsService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameMediaResource;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.jobs.JobService;
import de.mephisto.vpin.server.recorder.RecorderService;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

import static de.mephisto.vpin.server.mcp.McpManagementTools.enumProp;
import static de.mephisto.vpin.server.mcp.McpTableTools.prop;
import static de.mephisto.vpin.server.mcp.McpTableTools.schema;

/**
 * MCP tools for the frontend media of the tables: searching the media sources, downloading and deleting media,
 * recording screens and following the background jobs these start.
 */
@Service
public class McpMediaTools implements McpToolProvider {
  private final static Logger LOG = LoggerFactory.getLogger(McpMediaTools.class);

  private static final int MAX_RESULTS = 25;
  private static final int MAX_CACHED_ASSETS = 500;

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private TableAssetsService tableAssetsService;

  @Autowired
  private TableAssetSourcesService tableAssetSourcesService;

  @Autowired
  private RecorderService recorderService;

  @Autowired
  private JobService jobService;

  @Lazy
  @Autowired
  private GameMediaResource gameMediaResource;

  /**
   * The assets found by the latest searches, the AI refers to them by id when downloading.
   */
  private final Map<String, TableAsset> foundAssets = Collections.synchronizedMap(new LinkedHashMap<>() {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, TableAsset> eldest) {
      return size() > MAX_CACHED_ASSETS;
    }
  });

  @Override
  public List<McpTool> getTools() {
    List<McpTool> tools = new ArrayList<>();
    List<String> screens = Arrays.stream(VPinScreen.values()).map(Enum::name).toList();

    tools.add(McpTool.read("search_media_sources", "Search media sources",
        "Searches the configured media sources (e.g. the frontend media database) for media of a table and screen. "
            + "Returns the found media with an assetId for download_media.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "screen", enumProp("The screen, e.g. Wheel, PlayField, BackGlass, DMD, Topper.", screens),
            "term", prop("string", "Search term, default the table name."),
            "sourceId", prop("string", "Only search this media source, see list_settings key assetSourcesSettings.")), List.of("id", "screen")),
        this::searchMedia));

    tools.add(McpTool.read("list_jobs", "List jobs",
        "Lists the background jobs of the server (downloads, recordings, backups, imports) with their progress.",
        schema(Map.of("onlyActive", prop("boolean", "Only jobs that are not finished, default false.")), List.of()),
        this::listJobs));

    tools.add(McpTool.read("get_job", "Get job",
        "Returns the status of one background job.",
        schema(Map.of("jobId", prop("string", "The job id, see list_jobs.")), List.of("jobId")),
        this::getJob));

    tools.add(McpTool.write(McpToolGroup.MEDIA, "download_media", "Download media",
        "Downloads a media file found by search_media_sources and adds it to the frontend media of the table.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "screen", enumProp("The screen the media is added to.", screens),
            "assetId", prop("string", "The assetId returned by search_media_sources."),
            "append", prop("boolean", "Keep the existing media of the screen and add this one, default false (the existing media is replaced).")),
            List.of("id", "screen", "assetId")),
        this::downloadMedia));

    tools.add(McpTool.delete(McpToolGroup.MEDIA, "delete_media", "Delete media",
        "Deletes one frontend media file of a table (see get_table_assets, frontendMedia). This cannot be undone, "
            + "so confirm with the user first.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "screen", enumProp("The screen.", screens),
            "file", prop("string", "The file name as returned by get_table_assets.")), List.of("id", "screen", "file")),
        this::deleteMedia));

    Map<String, Object> tableIds = prop("array", "The table ids, see search_tables.");
    tableIds.put("items", Map.of("type", "integer"));
    Map<String, Object> screenList = prop("array", "The screens to record, see the recorder settings for the available ones. Default all recording screens.");
    screenList.put("items", Map.of("type", "string", "enum", screens));
    tools.add(McpTool.write(McpToolGroup.MEDIA, "start_recording", "Record media",
        "Records videos of the given screens for each table: every table is launched in turn and recorded. The "
            + "existing media of the screens is replaced. This takes a while and blocks the cabinet; confirm with the "
            + "user first. Returns a job id for get_job.",
        schema(Map.of("tableIds", tableIds, "screens", screenList), List.of("tableIds")),
        this::startRecording));

    tools.add(McpTool.write(McpToolGroup.MEDIA, "cancel_job", "Cancel job",
        "Cancels a running background job.",
        schema(Map.of("jobId", prop("string", "The job id, see list_jobs.")), List.of("jobId")),
        this::cancelJob));
    return tools;
  }

  //-------------------------------- read -------------------------------------------------------------

  private Object searchMedia(McpArguments args) {
    requireMedia();
    Game game = requireGame(args.requireInt("id"));
    VPinScreen screen = requireScreen(args.getString("screen"));
    String term = StringUtils.defaultIfBlank(args.getString("term"), game.getGameDisplayName());
    String sourceId = StringUtils.trimToNull(args.getString("sourceId"));
    TableAssetSource source = null;
    if (sourceId != null) {
      source = tableAssetSourcesService.getAssetSource(sourceId);
      if (source == null) {
        throw new McpToolException("Unknown media source \"" + sourceId + "\".");
      }
    }
    EmulatorType emulatorType = game.getEmulator() != null ? game.getEmulator().getType() : EmulatorType.VisualPinball;

    List<TableAsset> assets;
    try {
      assets = tableAssetsService.search(source, emulatorType, screen.getSegment(), game, term);
    }
    catch (Exception e) {
      throw new McpToolException("The media search failed: " + e.getMessage());
    }

    List<Map<String, Object>> found = new ArrayList<>();
    for (TableAsset asset : assets) {
      if (found.size() >= MAX_RESULTS) {
        break;
      }
      String assetId = UUID.nameUUIDFromBytes((asset.getSourceId() + "|" + asset.getUrl()).getBytes()).toString().substring(0, 8);
      foundAssets.put(assetId, asset);
      Map<String, Object> a = new LinkedHashMap<>();
      a.put("assetId", assetId);
      a.put("name", asset.getName());
      a.put("source", asset.getSourceId());
      a.put("mimeType", asset.getMimeType());
      a.put("author", asset.getAuthor());
      if (asset.getLength() > 0) {
        a.put("size", asset.getLength());
      }
      found.add(a);
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("screen", screen.name());
    result.put("term", term);
    result.put("total", assets.size());
    result.put("assets", found);
    result.put("existingMedia", frontendService.getMediaFiles(game, screen).stream().map(File::getName).toList());
    return result;
  }

  private Object listJobs(McpArguments args) {
    boolean onlyActive = args.getBoolean("onlyActive", false);
    return jobService.getJobs().stream().filter(j -> !onlyActive || !j.isFinished()).map(McpMediaTools::toJob).toList();
  }

  private Object getJob(McpArguments args) {
    return toJob(requireJob(args.getString("jobId")));
  }

  //-------------------------------- write ------------------------------------------------------------

  private Object downloadMedia(McpArguments args) {
    requireMedia();
    Game game = requireGame(args.requireInt("id"));
    VPinScreen screen = requireScreen(args.getString("screen"));
    String assetId = StringUtils.trimToEmpty(args.getString("assetId"));
    TableAsset asset = foundAssets.get(assetId);
    if (asset == null) {
      throw new McpToolException("Unknown assetId \"" + assetId + "\", search again with search_media_sources.");
    }
    boolean append = args.getBoolean("append", false);
    try {
      gameMediaResource.downloadTableAsset(game.getId(), screen, append, VPinScreen.Loading, asset);
    }
    catch (Exception e) {
      throw new McpToolException("The download failed: " + e.getMessage());
    }
    LOG.info("MCP: downloaded \"{}\" as {} media of \"{}\"", asset.getName(), screen, game.getGameDisplayName());
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("screen", screen.name());
    result.put("downloaded", asset.getName());
    result.put("media", frontendService.getMediaFiles(game, screen).stream().map(File::getName).toList());
    return result;
  }

  private Object deleteMedia(McpArguments args) {
    requireMedia();
    Game game = requireGame(args.requireInt("id"));
    VPinScreen screen = requireScreen(args.getString("screen"));
    String file = StringUtils.trimToEmpty(args.getString("file"));
    List<String> existing = frontendService.getMediaFiles(game, screen).stream().map(File::getName).toList();
    if (!existing.contains(file)) {
      throw new McpToolException("\"" + file + "\" is not a " + screen.name() + " media file of this table. Existing files: " + existing);
    }
    boolean deleted = gameMediaResource.deleteMedia(game.getId(), screen, file);
    LOG.info("MCP: deleted {} media \"{}\" of \"{}\"", screen, file, game.getGameDisplayName());
    return Map.of("tableId", game.getId(), "screen", screen.name(), "file", file, "deleted", deleted);
  }

  private Object startRecording(McpArguments args) {
    if (!VPinStudioServer.Features.RECORDER) {
      throw new McpToolException("The media recorder is not available with the frontend of this cabinet.");
    }
    if (recorderService.isRecording() != null) {
      throw new McpToolException("A recording is already running, see list_jobs.");
    }
    List<Integer> ids = args.getIntList("tableIds");
    if (ids.isEmpty()) {
      throw new McpToolException("Missing argument \"tableIds\".");
    }
    List<VPinScreen> available = recorderService.getRecordingScreens().stream().map(FrontendPlayerDisplay::getScreen).filter(Objects::nonNull).toList();
    List<VPinScreen> screens = new ArrayList<>();
    for (String name : args.getStringList("screens")) {
      VPinScreen screen = requireScreen(name);
      if (!available.contains(screen)) {
        throw new McpToolException("The screen " + screen.name() + " can not be recorded. Recording screens: " + available);
      }
      screens.add(screen);
    }
    if (screens.isEmpty()) {
      screens.addAll(available);
    }

    RecordingDataSummary summary = new RecordingDataSummary();
    for (Integer id : new LinkedHashSet<>(ids)) {
      Game game = requireGame(id);
      RecordingData data = new RecordingData();
      data.setGameId(game.getId());
      data.setScreens(new ArrayList<>(screens));
      summary.add(data);
    }
    JobDescriptor job = recorderService.startRecording(summary);
    LOG.info("MCP: started recording of {} tables", summary.size());
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("jobId", job.getUuid());
    result.put("tables", summary.getGameIds());
    result.put("screens", screens.stream().map(Enum::name).toList());
    return result;
  }

  private Object cancelJob(McpArguments args) {
    JobDescriptor job = requireJob(args.getString("jobId"));
    if (job.isFinished()) {
      throw new McpToolException("The job is already finished.");
    }
    if (!job.isCancelable()) {
      throw new McpToolException("The job \"" + job.getTitle() + "\" can not be cancelled.");
    }
    jobService.cancel(job.getUuid());
    LOG.info("MCP: cancelled job \"{}\"", job.getTitle());
    return Map.of("jobId", job.getUuid(), "cancelled", true);
  }

  //-------------------------------- helpers ----------------------------------------------------------

  private static void requireMedia() {
    if (!VPinStudioServer.Features.MEDIA_ENABLED) {
      throw new McpToolException("The frontend of this cabinet has no media management.");
    }
  }

  private Game requireGame(int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new McpToolException("No table found for id " + id + ", use search_tables to look up the id.");
    }
    return game;
  }

  private VPinScreen requireScreen(String value) {
    String name = StringUtils.trimToNull(value);
    if (name == null) {
      throw new McpToolException("Missing argument \"screen\".");
    }
    for (VPinScreen screen : frontendService.getFrontend().getSupportedScreens()) {
      if (screen.name().equalsIgnoreCase(name) || screen.getSegment().equalsIgnoreCase(name)) {
        return screen;
      }
    }
    throw new McpToolException("Unknown or unsupported screen \"" + value + "\". The frontend supports: "
        + frontendService.getFrontend().getSupportedScreens());
  }

  private JobDescriptor requireJob(String jobId) {
    JobDescriptor job = StringUtils.isNotBlank(jobId) ? jobService.getJob(jobId.trim()) : null;
    if (job == null) {
      throw new McpToolException("No job found for id \"" + jobId + "\", see list_jobs.");
    }
    return job;
  }

  private static Map<String, Object> toJob(JobDescriptor job) {
    Map<String, Object> j = new LinkedHashMap<>();
    j.put("jobId", job.getUuid());
    j.put("type", job.getJobType() != null ? job.getJobType().name() : null);
    j.put("title", job.getTitle());
    j.put("status", job.getStatus());
    j.put("progress", Math.round(job.getProgress() * 100));
    j.put("finished", job.isFinished());
    j.put("cancelled", job.isCancelled());
    j.put("cancelable", job.isCancelable());
    if (job.getGameId() > 0) {
      j.put("tableId", job.getGameId());
    }
    if (StringUtils.isNotEmpty(job.getError())) {
      j.put("error", job.getError());
    }
    return j;
  }
}
