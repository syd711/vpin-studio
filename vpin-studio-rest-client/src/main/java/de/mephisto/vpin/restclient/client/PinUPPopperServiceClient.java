package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.ResourceList;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public ResourceList getImportableTables() {
    return getRestClient().get(API + "popper/imports", ResourceList.class);
  }

  public JobExecutionResult importTables(ResourceList resourceList) throws Exception {
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

}
