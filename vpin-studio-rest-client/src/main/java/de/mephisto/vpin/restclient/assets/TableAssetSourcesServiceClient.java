package de.mephisto.vpin.restclient.assets;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Table Asset Sources
 ********************************************************************************************************************/
public class TableAssetSourcesServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public TableAssetSourcesServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<TableAssetSource> getAssetSource(String id) {
    return Arrays.asList(getRestClient().get(API + "assetsources/" + id, TableAssetSource[].class));
  }

  @Nullable
  public TableAssetSource getDefaultAssetSource() {
    return getRestClient().get(API + "assetsources/default", TableAssetSource.class);
  }

  public List<TableAssetSource> getAssetSources() {
    return Arrays.asList(getRestClient().get(API + "assetsources", TableAssetSource[].class));
  }

  public boolean deleteAssetSource(String id) {
    return getRestClient().delete(API + "assetsources/" + id);
  }

  public TableAssetSource saveAssetSource(TableAssetSource source) throws Exception {
    try {
      return getRestClient().post(API + "assetsources/save", source, TableAssetSource.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save archive source: " + e.getMessage(), e);
      throw e;
    }
  }
}
