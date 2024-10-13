package de.mephisto.vpin.restclient.recorder;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Recorder
 ********************************************************************************************************************/
public class RecorderServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public RecorderServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<RecordingScreen> getRecordingScreens() {
    return Arrays.asList(getRestClient().get(API + "recorder/screens", RecordingScreen[].class));
  }

  public RecordingData startRecording(RecordingData status) {
    try {
      return getRestClient().post(API + "recorder/start", status, RecordingData.class);
    }
    catch (Exception e) {
      LOG.error("Failed to start recording: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean stopRecording() {
    return getRestClient().get(API + "recorder/stop", Boolean.class);
  }
}
