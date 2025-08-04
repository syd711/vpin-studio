package de.mephisto.vpin.restclient.mediasources;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Media Sources
 ********************************************************************************************************************/
public class MediaSourcesServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public MediaSourcesServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<MediaSourceRepresentation> getMediaSource(long id) {
    return Arrays.asList(getRestClient().get(API + "mediasources/" + id, MediaSourceRepresentation[].class));
  }

  public List<MediaSourceRepresentation> getMediaSources() {
    return Arrays.asList(getRestClient().get(API + "mediasources", MediaSourceRepresentation[].class));
  }

  public boolean deleteMediaSource(long id) {
    return getRestClient().delete(API + "mediasources/" + id);
  }

  public MediaSourceRepresentation saveMediaSource(MediaSourceRepresentation source) throws Exception {
    try {
      return getRestClient().post(API + "mediasources/save", source, MediaSourceRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save archive source: " + e.getMessage(), e);
      throw e;
    }
  }
}
