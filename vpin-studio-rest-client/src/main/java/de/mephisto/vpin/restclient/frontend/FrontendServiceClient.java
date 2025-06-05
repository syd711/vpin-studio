package de.mephisto.vpin.restclient.frontend;

import de.mephisto.vpin.connectors.vps.matcher.VpsMatch;
import de.mephisto.vpin.restclient.DatabaseLockException;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.*;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*********************************************************************************************************************
 * Frontend
 ********************************************************************************************************************/
public class FrontendServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  private static final String API_SEGMENT_FRONTEND = "frontend";

  private FrontendType frontendType;

  public FrontendServiceClient(VPinStudioClient client) {
    super(client);
  }


  public int getVersion() {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/version", Integer.class);
  }

  public Frontend getFrontendCached() {
    return getRestClient().getCached(API + API_SEGMENT_FRONTEND, Frontend.class);
  }

  public Frontend getFrontend() {
    return getRestClient().get(API + API_SEGMENT_FRONTEND, Frontend.class);
  }

  public FrontendType getFrontendType() {
    if (frontendType == null) {
      frontendType = getFrontendCached().getFrontendType();
    }
    return frontendType;
  }

  public GameList getImportableTablesVpx() {
    GameList games = new GameList();
    List<GameEmulatorRepresentation> gameEmulators = client.getEmulatorService().getValidatedGameEmulators();
    for (GameEmulatorRepresentation gameEmulator : gameEmulators) {
      if (gameEmulator.isVpxEmulator()) {
        GameList l = getImportableTables(gameEmulator.getId());
        games.addItems(l.getItems());
      }
    }
    return games;
  }

  public GameList getImportableTables(int emulatorId) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/imports/" + emulatorId, GameList.class);
  }

  public JobDescriptor importTable(GameListItem item) throws Exception {
    try {
      return getRestClient().post(API + API_SEGMENT_FRONTEND + "/import", item, JobDescriptor.class);
    }
    catch (Exception e) {
      LOG.error("Failed importing tables: " + e.getMessage(), e);
      throw e;
    }
  }

  public FrontendControl getPinUPControlFor(VPinScreen screen) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/pincontrol/" + screen.name(), FrontendControl.class);
  }

  public FrontendMediaRepresentation getFrontendMedia(int gameId) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/media/" + gameId, FrontendMediaRepresentation.class);
  }
  public FrontendMediaItemRepresentation getDefaultFrontendMediaItem(int gameId, VPinScreen screen) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/media/" + gameId + "/" + screen.name(), 
      FrontendMediaItemRepresentation.class);
  }

  public FrontendPlayerDisplay getScreenDisplay(VPinScreen screen) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/screen/" + screen.name(), FrontendPlayerDisplay.class);
  }

  public FrontendScreenSummary getScreenSummary(boolean forceReload) {
    if(forceReload) {
      getRestClient().clearCache(API + API_SEGMENT_FRONTEND + "/screens");
    }
    return getRestClient().getCached(API + API_SEGMENT_FRONTEND + "/screens", FrontendScreenSummary.class);
  }

  public FrontendControls getPinUPControls() {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/pincontrols", FrontendControls.class);
  }

  public boolean isFrontendRunning() {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/running", Boolean.class);
  }

  public boolean launchGame(int gameId) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/launch/" + gameId, Boolean.class);
  }

  public boolean terminateFrontend() {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/terminate", Boolean.class);
  }

  public boolean restartFrontend() {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/restart", Boolean.class);
  }

  public TableDetails getTableDetails(int gameId) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/tabledetails/" + gameId, TableDetails.class);
  }

  public TableDetails saveTableDetails(TableDetails tableDetails, int gameId) throws Exception {
    try {
      return getRestClient().post(API + API_SEGMENT_FRONTEND + "/tabledetails/" + gameId, tableDetails, TableDetails.class);
    }
    catch (Exception e) {
      LOG.error("Failed save table details: " + e.getMessage(), e);
      throw e;
    }
  }

  public File getMediaDirectory(int gameId, String screen) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/mediadir/" + gameId + "/" + screen, File.class);
  }

  public File getPlaylistMediaDirectory(int playlistId, String screen) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/playlistmediadir/" + playlistId + "/" + screen, File.class);
  }

  public TableDetails autoFillTableDetails(int gameId) throws Exception {
    try {
      return getRestClient().put(API + API_SEGMENT_FRONTEND + "/tabledetails/autofill/" + gameId, Collections.emptyMap(), TableDetails.class);
    }
    catch (Exception e) {
      LOG.error("Failed autofilling table details: " + e.getMessage(), e);
      throw e;
    }
  }

  public TableDetails autoFillTableDetailsSimulated(int gameId, TableDetails tableDetails, String vpsTableId, String vpsVersionId) throws Exception {
    try {
      if (StringUtils.isEmpty(vpsTableId)) {
        vpsTableId = "-";
      }
      if (StringUtils.isEmpty(vpsVersionId)) {
        vpsVersionId = "-";
      }
      return getRestClient().post(API + API_SEGMENT_FRONTEND + "/tabledetails/autofillsimulate/" + vpsTableId + "/" + vpsVersionId + "/" + gameId, tableDetails, TableDetails.class);
    }
    catch (Exception e) {
      LOG.error("Failed simulating autofilling table details: " + e.getMessage(), e);
      throw e;
    }
  }

  public VpsMatch autoMatch(int gameId, boolean overwrite, boolean simulate) {
    if (simulate) {
      return getRestClient().get(API + API_SEGMENT_FRONTEND + "/tabledetails/automatchsimulate/" + gameId + "/" + overwrite, VpsMatch.class);
    }
    else {
      return getRestClient().get(API + API_SEGMENT_FRONTEND + "/tabledetails/automatch/" + gameId + "/" + overwrite, VpsMatch.class);
    }
  }

  public void saveVpsMapping(int gameId, String extTableId, String extTableVersionId) throws Exception {
    VpsMatch vpsMatch = new VpsMatch();
    vpsMatch.setGameId(gameId);
    vpsMatch.setExtTableId(extTableId);
    vpsMatch.setExtTableVersionId(extTableVersionId);
    getRestClient().post(API + API_SEGMENT_FRONTEND + "/tabledetails/vpsLink/" + gameId, vpsMatch, Boolean.class);
  }

  public void fixVersion(int gameId, String version) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("version", version);
    getRestClient().put(API + API_SEGMENT_FRONTEND + "/tabledetails/fixVersion/" + gameId, params, Boolean.class);
  }


  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + API_SEGMENT_FRONTEND + "/clearcache", Boolean.class);
  }

  //-----------------------------

  public void saveSettings(JsonSettings settings) throws Exception {
    try {
      getRestClient().post(API + API_SEGMENT_FRONTEND + "/settings", settings, Boolean.class);
    }
    catch (HttpClientErrorException e) {
      if (e.getStatusCode().is4xxClientError()) {
        throw new DatabaseLockException(e);
      }
      throw e;
    }
    catch (Exception e) {
      LOG.error("Failed save custom options: " + e.getMessage(), e);
      throw e;
    }
  }

  public <T> T getSettings(Class<T> clazz) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/settings", clazz);
  }

  public void reload() {
    getRestClient().clearCache("frontend/emulators");
    getRestClient().clearCache("frontend");
  }
}
