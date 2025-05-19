package de.mephisto.vpin.restclient.converter;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MediaConversionServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MediaConversionServiceClient.class);

  public MediaConversionServiceClient(VPinStudioClient client) {
    super(client);
  }

  public MediaOperationResult convert(int gameId, VPinScreen screen, String name, MediaConversionCommand command) {
    try {
      MediaOperation mediaOperation = new MediaOperation();
      mediaOperation.setScreen(screen);
      mediaOperation.setGameId(gameId);
      mediaOperation.setFilename(name);
      mediaOperation.setCommand(command);
      return getRestClient().post(API + "convertmedia/batch", mediaOperation, MediaOperationResult.class);
    }
    catch (Exception e) {
      LOG.error("Failed to convert video: " + e.getMessage(), e);
    }
    return null;
  }

  public List<MediaConversionCommand> getCommandList() {
    try {
      return new ArrayList<>(Arrays.asList(getRestClient().get(API + "convertmedia/commands", MediaConversionCommand[].class))); 
    }
    catch (Exception e) {
      LOG.error("Failed to load commands, return empty command list: " + e.getMessage());
      return Collections.emptyList();
    }
  }

}
