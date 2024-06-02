package de.mephisto.vpin.restclient.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/*********************************************************************************************************************
 * VPS Service
 ********************************************************************************************************************/
public class VpsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VpsServiceClient.class);

  public VpsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public Map<String, VpsTable> cache = new LinkedHashMap<>();

  public List<VpsTable> getTables() {
    return new ArrayList<>(cache.values());
  }

  public VpsTable getTableById(String extTableId) {
    if (extTableId == null) {
      return null;
    }
    if (!cache.containsKey(extTableId)) {
      VpsTable vpsTable = getRestClient().get(API + "vps/vpsTable/" + extTableId, VpsTable.class);
      cache.put(extTableId, vpsTable);
    }

    return cache.get(extTableId);
  }

  public List<VpsTable> find(String term, String rom) {
    return Arrays.asList(getRestClient().get(API + "vps/find/" + rom + "/" + term, VpsTable[].class));
  }

  public boolean update() {
    getRestClient().get(API + "vps/update", Boolean.class);
    invalidateAll();
    return true;
  }

  public void invalidateAll() {
    List<VpsTable> list = Arrays.asList(getRestClient().get(API + "vps/vpsTables", VpsTable[].class));
    for (VpsTable cachedTable : list) {
      cache.put(cachedTable.getId(), cachedTable);
    }
    LOG.info("Loaded " + cache.size() + " mapped VPS tables.");
  }

  public Date getChangeDate() {
    return getRestClient().get(API + "vps/changeDate", Date.class);
  }

}
