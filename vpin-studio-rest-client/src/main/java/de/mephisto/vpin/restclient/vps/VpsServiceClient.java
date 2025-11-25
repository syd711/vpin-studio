package de.mephisto.vpin.restclient.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/*********************************************************************************************************************
 * VPS Service
 ********************************************************************************************************************/
public class VpsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public VpsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public Map<String, VpsTable> cache = new LinkedHashMap<>();

  public List<VpsTable> getTables() {
    List<VpsTable> vpsTables = new ArrayList<>(cache.values());
    List<VpsTable> filtered = vpsTables.stream().filter(t -> t != null && !StringUtils.isEmpty(t.getName())).collect(Collectors.toList());
    Collections.sort(filtered, Comparator.comparing(o -> String.valueOf(o.getName().trim().toLowerCase())));
    return filtered;
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

  /**
   * Redownload the database online and load it in the database
   */
  public boolean update() {
    getRestClient().get(API + "vps/update", Boolean.class);
    invalidateAll();
    return true;
  }

  /**
   * Reload the database from the local file
   */
  public boolean reload() {
    getRestClient().get(API + "vps/reload", Boolean.class);
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

  public List<VpsInstallLink> getInstallLinks(String link) {
    String encodedLink = URLEncoder.encode(link, StandardCharsets.UTF_8);    
    return Arrays.asList(getRestClient().get(API + "vps/installLinks/" + encodedLink, VpsInstallLink[].class));
  }

  public VpsTable saveVpsData(VpsTable vpsTable) {
    try {
      return getRestClient().post(API + "vps/save", vpsTable, VpsTable.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save VpsTable: " + e.getMessage(), e);
      throw e;
    }
  }
}
