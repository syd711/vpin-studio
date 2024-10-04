package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

/**
 *
 */
@RestController
@RequestMapping(API_SEGMENT + "recorder")
public class RecorderResource {
  private final static Logger LOG = LoggerFactory.getLogger(RecorderResource.class);

  @Autowired
  private RecorderService recorderService;

  @GetMapping("/screens")
  public List<RecordingScreen> getRecordingScreens() {
    return recorderService.getRecordingScreens();
  }
}
