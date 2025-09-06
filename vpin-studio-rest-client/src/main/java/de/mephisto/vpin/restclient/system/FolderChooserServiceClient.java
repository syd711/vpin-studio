package de.mephisto.vpin.restclient.system;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Folder selection
 ********************************************************************************************************************/
public class FolderChooserServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public FolderChooserServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<FolderRepresentation> getRoots() {
    return Arrays.asList(getRestClient().get(API + "folder/roots", FolderRepresentation[].class));
  }

  public FolderRepresentation getFolder(@Nullable String path) {
    if (path == null) {
      return getRestClient().get(API + "folder", FolderRepresentation.class);
    }
    return getRestClient().get(API + "folder?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8), FolderRepresentation.class);
  }
}
