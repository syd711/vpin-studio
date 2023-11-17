package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.DatabaseLockException;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.popper.*;
import de.mephisto.vpin.restclient.tables.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.tables.GameList;
import de.mephisto.vpin.restclient.tables.GameMediaRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

/*********************************************************************************************************************
 * Popper
 ********************************************************************************************************************/
public class PinUPPopperServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  PinUPPopperServiceClient(VPinStudioClient client) {
    super(client);
  }

  public GameList getImportableTables() {
    return getRestClient().get(API + "popper/imports", GameList.class);
  }

  public JobExecutionResult importTables(GameList list) throws Exception {
    try {
      return getRestClient().post(API + "popper/import", list, JobExecutionResult.class);
    } catch (Exception e) {
      LOG.error("Failed importing tables: " + e.getMessage(), e);
      throw e;
    }
  }

  public PinUPControl getPinUPControlFor(PopperScreen screen) {
    return getRestClient().get(API + "popper/pincontrol/" + screen.name(), PinUPControl.class);
  }

  public GameEmulatorRepresentation getGameEmulator(int id) {
    List<GameEmulatorRepresentation> gameEmulators = getGameEmulators();
    return gameEmulators.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
  }

  public List<GameEmulatorRepresentation> getGameEmulators() {
    return Arrays.asList(getRestClient().getCached(API + "popper/emulators", GameEmulatorRepresentation[].class));
  }

  public List<GameEmulatorRepresentation> getBackglassGameEmulators() {
    return Arrays.asList(getRestClient().getCached(API + "popper/backglassemulators", GameEmulatorRepresentation[].class));
  }

  public PinUPControls getPinUPControls() {
    return getRestClient().get(API + "popper/pincontrols", PinUPControls.class);
  }

  public boolean isPinUPPopperRunning() {
    return getRestClient().get(API + "popper/running", Boolean.class);
  }

  public boolean terminatePopper() {
    return getRestClient().get(API + "popper/terminate", Boolean.class);
  }

  public boolean restartPopper() {
    return getRestClient().get(API + "popper/restart", Boolean.class);
  }

  public TableDetails getTableDetails(int gameId) {
    return getRestClient().get(API + "popper/tabledetails/" + gameId, TableDetails.class);
  }

  public TableDetails saveTableDetails(TableDetails tableDetails, int gameId) throws Exception {
    try {
      return getRestClient().post(API + "popper/tabledetails/" + gameId, tableDetails, TableDetails.class);
    } catch (Exception e) {
      LOG.error("Failed save table details: " + e.getMessage(), e);
      throw e;
    }
  }

  public TableDetails autoFillTableDetails(int gameId, boolean checked) throws Exception {
    try {
      return getRestClient().put(API + "popper/tabledetails/autofill/" + gameId + "/" + (checked ? "1" : "0"), Collections.emptyMap(), TableDetails.class);
    } catch (Exception e) {
      LOG.error("Failed autofilling table details: " + e.getMessage(), e);
      throw e;
    }
  }

  public PopperCustomOptions saveCustomOptions(PopperCustomOptions options) throws Exception {
    try {
      return getRestClient().post(API + "popper/custompoptions", options, PopperCustomOptions.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode().is4xxClientError()) {
        throw new DatabaseLockException(e);
      }
      throw e;
    } catch (Exception e) {
      LOG.error("Failed save custom options: " + e.getMessage(), e);
      throw e;
    }
  }

  public PopperCustomOptions getCustomOptions() {
    return getRestClient().get(API + "popper/custompoptions", PopperCustomOptions.class);
  }

  public boolean deleteMedia(int gameId, PopperScreen screen, String name) {
    return getRestClient().delete(API + "poppermedia/media/" + gameId + "/" + screen.name() + "/" + name);
  }

  public GameMediaRepresentation getGameMedia(int gameId) {
    return getRestClient().get(API + "poppermedia/" + gameId, GameMediaRepresentation.class);
  }


  public JobExecutionResult uploadMedia(File file, String uploadType, int gameId, PopperScreen screen, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "poppermedia/upload/" + screen.name();
      ResponseEntity<JobExecutionResult> exchange = new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, uploadType, AssetType.POPPER_MEDIA, listener), JobExecutionResult.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("Popper media upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean toFullScreen(int gameId, PopperScreen screen) throws Exception {
    try {
      Map<String, Object> values = new HashMap<>();
      values.put("fullscreen", "true");
      return getRestClient().put(API + "poppermedia/media/" + gameId + "/" + screen.name(), values);
    } catch (Exception e) {
      LOG.error("Applying fullscreen mode failed: " + e.getMessage(), e);
      throw e;
    }
  }

  //---------------- Assets---------------------------------------------------------------------------------------------

  public TableAssetSearch searchTableAsset(PopperScreen screen, String term) throws Exception {
    TableAssetSearch search = new TableAssetSearch();
    search.setTerm(term);
    search.setScreen(screen);
    return getRestClient().post(API + "poppermedia/assets/search", search, TableAssetSearch.class);
  }

  public boolean downloadTableAsset(TableAsset tableAsset, PopperScreen screen, GameRepresentation game, boolean append) throws Exception {
    try {
      return getRestClient().post(API + "poppermedia/assets/download/" + game.getId() + "/" + screen.name() + "/" + append, tableAsset, Boolean.class);
    } catch (Exception e) {
      LOG.error("Failed to save b2s server settings: " + e.getMessage(), e);
      throw e;
    }
  }
}
