package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.server.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

  @GetMapping("/preview/{screen}")
  public ResponseEntity<byte[]> preview(@PathVariable("screen") VPinScreen screen) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    recorderService.refreshPreview(out, screen);
    return RequestUtil.serializeImage(out.toByteArray(), screen.name() + ".jpg");
  }
}
