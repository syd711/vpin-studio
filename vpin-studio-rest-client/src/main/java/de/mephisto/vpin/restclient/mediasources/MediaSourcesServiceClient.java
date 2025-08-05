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

  public List<MediaSource> getMediaSource(String id) {
    return Arrays.asList(getRestClient().get(API + "mediasources/" + id, MediaSource[].class));
  }

  public List<MediaSource> getMediaSources() {
    return Arrays.asList(getRestClient().get(API + "mediasources", MediaSource[].class));
  }

  public boolean deleteMediaSource(String id) {
    return getRestClient().delete(API + "mediasources/" + id);
  }

  public MediaSource saveMediaSource(MediaSource source) throws Exception {
    try {
      return getRestClient().post(API + "mediasources/save", source, MediaSource.class);
    } catch (Exception e) {
      LOG.error("Failed to save archive source: " + e.getMessage(), e);
      throw e;
    }
  }
}
