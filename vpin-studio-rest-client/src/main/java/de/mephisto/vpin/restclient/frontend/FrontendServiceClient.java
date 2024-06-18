package de.mephisto.vpin.restclient.frontend;

import de.mephisto.vpin.restclient.DatabaseLockException;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.GameVpsMatch;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collectors;

/*********************************************************************************************************************
 * Frontend
 ********************************************************************************************************************/
public class FrontendServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  private static final String API_SEGMENT_FRONTEND = "frontend";

  private List<TableAssetSearch> cache = new ArrayList<>();

  private Frontend frontend;

  public FrontendServiceClient(VPinStudioClient client) {
    super(client);
  }


  public int getVersion() {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/version", Integer.class);
  }

  public Frontend getFrontend() {
    if (frontend == null) {
      frontend = getRestClient().get(API + API_SEGMENT_FRONTEND, Frontend.class);
    }
    return frontend;
  }

  public FrontendType getFrontendType() {
    if (frontend == null) {
      getFrontend();
    }
    return frontend.getFrontendType();
  }

  public GameList getImportableTables() {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/imports", GameList.class);
  }

  public JobExecutionResult importTable(GameListItem item) throws Exception {
    try {
      return getRestClient().post(API + API_SEGMENT_FRONTEND + "/import", item, JobExecutionResult.class);
    }
    catch (Exception e) {
      LOG.error("Failed importing tables: " + e.getMessage(), e);
      throw e;
    }
  }

  public FrontendControl getPinUPControlFor(VPinScreen screen) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/pincontrol/" + screen.name(), FrontendControl.class);
  }

  public GameEmulatorRepresentation getGameEmulator(int id) {
    List<GameEmulatorRepresentation> gameEmulators = getGameEmulators();
    return gameEmulators.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
  }

  public GameEmulatorRepresentation getDefaultGameEmulator() {
    List<GameEmulatorRepresentation> gameEmulators = getGameEmulators();
    return gameEmulators.size() > 0 ? gameEmulators.get(0) : null;
  }

  public FrontendPlayerDisplay getScreenDisplay(VPinScreen screen) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/screen/" + screen.name(), FrontendPlayerDisplay.class);
  }

  public List<FrontendPlayerDisplay> getScreenDisplays() {
    return Arrays.asList(getRestClient().get(API + API_SEGMENT_FRONTEND + "/screens", FrontendPlayerDisplay[].class));
  }

  public List<GameEmulatorRepresentation> getGameEmulators() {
    return Arrays.asList(getRestClient().getCached(API + API_SEGMENT_FRONTEND + "/emulators", GameEmulatorRepresentation[].class));
  }

  public List<GameEmulatorRepresentation> getVpxGameEmulators() {
    List<GameEmulatorRepresentation> gameEmulators = getGameEmulators();
    return gameEmulators.stream().filter(e -> e.isVpxEmulator()).collect(Collectors.toList());
  }

  public List<GameEmulatorRepresentation> getGameEmulatorsUncached() {
    return Arrays.asList(getRestClient().get(API + API_SEGMENT_FRONTEND + "/emulators", GameEmulatorRepresentation[].class));
  }

  public List<GameEmulatorRepresentation> getBackglassGameEmulators() {
    return Arrays.asList(getRestClient().getCached(API + API_SEGMENT_FRONTEND + "/backglassemulators", GameEmulatorRepresentation[].class));
  }

  public FrontendControls getPinUPControls() {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/pincontrols", FrontendControls.class);
  }

  public boolean isFrontendRunning() {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/running", Boolean.class);
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

  public TableDetails autoFillTableDetails(int gameId, boolean overwrite) throws Exception {
    try {
      return getRestClient().put(API + API_SEGMENT_FRONTEND + "/tabledetails/autofill/" + gameId + "/" + overwrite, Collections.emptyMap(), TableDetails.class);
    }
    catch (Exception e) {
      LOG.error("Failed autofilling table details: " + e.getMessage(), e);
      throw e;
    }
  }

  public TableDetails autoFillTableDetails(int gameId, TableDetails tableDetails) throws Exception {
    try {
      return getRestClient().post(API + API_SEGMENT_FRONTEND + "/tabledetails/autofillsimulate/" + gameId, tableDetails, TableDetails.class);
    }
    catch (Exception e) {
      LOG.error("Failed simulating autofilling table details: " + e.getMessage(), e);
      throw e;
    }
  }

  public GameVpsMatch autoMatch(int gameId, boolean overwrite) {
    return getRestClient().get(API + API_SEGMENT_FRONTEND + "/tabledetails/automatch/" + gameId + "/" + overwrite, GameVpsMatch.class);
  }

  public void vpsLink(int gameId, String extTableId, String extTableVersionId) throws Exception {
    GameVpsMatch vpsMatch = new GameVpsMatch();
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

  public void clearCache() {
    getRestClient().clearCache("frontend/emulators");
    this.cache.clear();
  }
}
