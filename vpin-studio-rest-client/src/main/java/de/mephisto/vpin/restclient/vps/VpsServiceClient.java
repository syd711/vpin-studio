package de.mephisto.vpin.restclient.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/*********************************************************************************************************************
 * VPS Service
 ********************************************************************************************************************/
public class VpsServiceClient extends VPinStudioClientService {

  public VpsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<VpsTable> getTables() {
    return Arrays.asList(getRestClient().get(API + "vps/vpsTables", VpsTable[].class));
  }

  public VpsTable getTableById(String extTableId) {
      if (extTableId==null) {
        return null;
      }
      return getRestClient().get(API + "vps/vpsTable/" + extTableId, VpsTable.class);
  }

  public List<VpsTable> find(String term, String rom) {
    return Arrays.asList(getRestClient().get(API + "vps/find/" + rom + "/" + term, VpsTable[].class));
  }

  public boolean update() {
    return getRestClient().get(API + "vps/update", Boolean.class);
  }

  public boolean reload() {
    return getRestClient().get(API + "vps/update", Boolean.class);
  }

  public Date getChangeDate() {
    return getRestClient().get(API + "vps/changeDate", Date.class);
  }

}
