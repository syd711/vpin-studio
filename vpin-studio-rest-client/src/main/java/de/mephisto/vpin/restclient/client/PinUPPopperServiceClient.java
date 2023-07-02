package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.restclient.FileUploadProgressListener;
import de.mephisto.vpin.restclient.SystemData;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Popper
 ********************************************************************************************************************/
public class PinUPPopperServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  PinUPPopperServiceClient(VPinStudioClient client) {
    super(client);
  }

  public SystemData getImportableTables() {
    return getRestClient().get(API + "popper/imports", SystemData.class);
  }

  public JobExecutionResult importTables(SystemData resourceList) throws Exception {
    try {
      return getRestClient().post(API + "popper/import", resourceList, JobExecutionResult.class);
    } catch (Exception e) {
      LOG.error("Failed importing tables: " + e.getMessage(), e);
      throw e;
    }
  }

  public PinUPControl getPinUPControlFor(PopperScreen screen) {
    return getRestClient().get(API + "popper/pincontrol/" + screen.name(), PinUPControl.class);
  }

  public PinUPControls getPinUPControls() {
    return getRestClient().get(API + "popper/pincontrols", PinUPControls.class);
  }

  public List<PlaylistRepresentation> getPlaylists() {
    return Arrays.asList(getRestClient().get(API + "popper/playlists", PlaylistRepresentation[].class));
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

  public JobExecutionResult uploadMedia(File file, String uploadType, int gameId, PopperScreen screen, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "popper/upload/" + screen.name();
      ResponseEntity<JobExecutionResult> exchange = new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, uploadType, AssetType.POPPER_MEDIA, listener), JobExecutionResult.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("Popper media upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
