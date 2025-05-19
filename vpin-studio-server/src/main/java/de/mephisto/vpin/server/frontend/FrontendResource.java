package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.connectors.vps.matcher.VpsMatch;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.playlists.Playlist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

/**
 *
 */
@RestController
@RequestMapping(API_SEGMENT + "frontend")
public class FrontendResource {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendResource.class);

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private VPinScreenService vpinScreenService;

  @Autowired
  private GameService gameService;

  @Autowired
  private GameMediaService gameMediaService;

  @GetMapping
  public Frontend getFrontend() {
    return frontendService.getFrontend();
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return frontendService.clearCache();
  }

  @GetMapping("/settings")
  public JsonSettings getSettings() {
    return frontendStatusService.getSettings();
  }

  @PostMapping("/settings")
  public boolean saveSettings(@RequestBody Map<String, Object> settings) {
    return frontendStatusService.saveSettings(settings);
  }

  @GetMapping("/media/{gameId}")
  public FrontendMedia getGameMedia(@PathVariable("gameId") int gameId) {
    return frontendService.getGameMedia(gameId);
  }
  @GetMapping("/media/{gameId}/{screen}")
  public FrontendMediaItem getDefaultMediaItem(@PathVariable("gameId") int gameId, @PathVariable("screen") VPinScreen screen) {
    FrontendMedia media = frontendService.getGameMedia(gameId);
    return media.getDefaultMediaItem(screen);
  }

  @GetMapping("/version")
  public int getVersion() {
    return frontendStatusService.getVersion();
  }

  @GetMapping("/imports/{emuId}")
  public GameList getImportTables(@PathVariable("emuId") int emuId) {
    return gameService.getImportableTables(emuId);
  }

  @GetMapping("/launch/{gameId}")
  public boolean launchGame(@PathVariable("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    return frontendService.launchGame(game);
  }

  @PostMapping("/import")
  public JobDescriptor importTable(@RequestBody GameListItem item) {
    File tableFile = new File(item.getFileName());
    if (tableFile.exists()) {
      int result = frontendService.importGame(tableFile, true, -1, item.getEmuId());
      if (result > 0) {
        gameService.scanGame(result);
      }
    }
    return JobDescriptorFactory.ok(-1);
  }

  @GetMapping("/pincontrol/{screen}")
  public FrontendControl getPinUPControlFor(@PathVariable("screen") String screenName) {
    return frontendStatusService.getPinUPControlFor(VPinScreen.valueOf(screenName));
  }

  @GetMapping("/pincontrols")
  public FrontendControls getPinUPControls() {
    return frontendStatusService.getPinUPControls();
  }


  @GetMapping("/running")
  public boolean isRunning() {
    return frontendService.isFrontendRunning();
  }

  @GetMapping("/terminate")
  public boolean terminate() {
    return frontendStatusService.terminate();
  }

  @GetMapping("/restart")
  public boolean restart() {
    return frontendStatusService.restart();
  }

  @GetMapping("/tabledetails/{gameId}")
  public TableDetails getTableDetails(@PathVariable("gameId") int gameId) {
    return gameMediaService.getTableDetails(gameId);
  }

  @GetMapping("/screen/{name}")
  public FrontendPlayerDisplay getScreen(@PathVariable("name") String name) {
    VPinScreen screen = VPinScreen.valueOf(name);
    return vpinScreenService.getScreenDisplay(screen);
  }

  @GetMapping("/screens")
  public FrontendScreenSummary getScreenSummary() {
    return vpinScreenService.getScreenSummary();
  }

  @GetMapping("/mediadir/{gameId}/{screenName}")
  public File getMediaDirectory(@PathVariable("gameId") int gameId, @PathVariable("screenName") String screenName) {
    VPinScreen screen = VPinScreen.valueOf(screenName);
    if (gameId < 0) {
      return frontendService.getDefaultMediaFolder(screen);
    }
    else {
      Game game = frontendService.getOriginalGame(gameId);
      return frontendService.getMediaFolder(game, screen, null);
    }
  }

  @GetMapping("/playlistmediadir/{playlistId}/{screenName}")
  public File getPlaylistMediaDirectory(@PathVariable("playlistId") int playlistId, @PathVariable("screenName") String name) {
    VPinScreen screen = VPinScreen.valueOf(name);
    Playlist playList = frontendService.getPlayList(playlistId);
    return frontendService.getPlaylistMediaFolder(playList, screen);
  }


  @PostMapping("/tabledetails/vpsLink/{gameId}")
  public boolean vpsLink(@PathVariable("gameId") int gameId, @RequestBody VpsMatch vpsmatch) throws Exception {
    gameService.vpsLink(gameId, vpsmatch.getExtTableId(), vpsmatch.getExtTableVersionId());
    return true;
  }

  @PutMapping("/tabledetails/fixVersion/{gameId}")
  public boolean fixVersion(@PathVariable("gameId") int gameId, @RequestBody Map<String, String> data) throws Exception {
    gameMediaService.fixGameVersion(gameId, data.get("version"), true);
    return true;
  }

  @PutMapping("/tabledetails/autofill/{gameId}")
  public TableDetails autofill(@PathVariable("gameId") int gameId) {
    TableDetails tableDetails = gameMediaService.getTableDetails(gameId);
    return frontendService.autoFill(gameService.getGame(gameId), tableDetails, false);
  }

  @PostMapping("/tabledetails/autofillsimulate/{vpsTableId}/{vpsVersionId}/{gameId}")
  public TableDetails autofill(@PathVariable("gameId") int gameId,
                               @PathVariable("vpsTableId") String vpsTableId,
                               @PathVariable("vpsVersionId") String vpsVersionId,
                               @RequestBody TableDetails tableDetails) {
    if (vpsTableId.equals("-")) {
      vpsTableId = null;
    }
    if (vpsVersionId.equals("-")) {
      vpsVersionId = null;
    }
    return frontendService.autoFill(gameService.getGame(gameId), tableDetails, vpsTableId, vpsVersionId, true);
  }

  @PostMapping("/tabledetails/{gameId}")
  public TableDetails save(@PathVariable("gameId") int gameId, @RequestBody TableDetails tableDetails) {
    return gameMediaService.saveTableDetails(tableDetails, gameId, true);
  }

  @GetMapping("/tabledetails/automatch/{gameId}/{overwrite}")
  public VpsMatch autoMatch(@PathVariable("gameId") int gameId, @PathVariable("overwrite") boolean overwrite) {
    return gameMediaService.autoMatch(gameService.getGame(gameId), overwrite, false);
  }

  @GetMapping("/tabledetails/automatchsimulate/{gameId}/{overwrite}")
  public VpsMatch autoMatchSimulate(@PathVariable("gameId") int gameId, @PathVariable("overwrite") boolean overwrite) {
    return gameMediaService.autoMatch(gameService.getGame(gameId), overwrite, true);
  }

}
