package de.mephisto.vpin.restclient.video;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoConversionServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VideoConversionServiceClient.class);

  public VideoConversionServiceClient(VPinStudioClient client) {
    super(client);
  }

  public Object convert(int gameId, VPinScreen screen, String name, VideoConversionCommand command) {
    try {
      VideoConversion videoConversion = new VideoConversion();
      videoConversion.setScreen(screen);
      videoConversion.setGameId(gameId);
      videoConversion.setName(name);
      videoConversion.setCommand(command);
      return getRestClient().post(API + "convertvideo/batch", videoConversion, Object.class);
    }
    catch (Exception e) {
      LOG.error("Failed to convert video: " + e.getMessage(), e);
    }
    return null;
  }

  public List<VideoConversionCommand> getCommandList() {
    return new ArrayList<>(Arrays.asList(getRestClient().get(API + "convertvideo/commands", VideoConversionCommand[].class)));
  }

}
