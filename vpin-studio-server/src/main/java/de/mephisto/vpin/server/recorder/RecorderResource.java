package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.server.util.RequestUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
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

  @Autowired
  private ScreenshotService screenshotService;

  @GetMapping("/screens")
  public List<FrontendPlayerDisplay> getRecordingScreens() {
    return recorderService.getRecordingScreens();
  }

  @GetMapping("/recording")
  public JobDescriptor isRecording() {
    return recorderService.isRecording();
  }

  @GetMapping("/preview/{screen}")
  public ResponseEntity<byte[]> preview(@PathVariable("screen") VPinScreen screen) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    recorderService.refreshPreview(out, screen);
    return RequestUtil.serializeImage(out.toByteArray(), screen.name() + ".jpg");
  }

  @GetMapping("/previewmonitor/{monitorId}")
  public ResponseEntity<byte[]> preview(@PathVariable("monitorId") int monitorId) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    recorderService.refreshPreview(out, monitorId);
    return RequestUtil.serializeImage(out.toByteArray(), "monitor-" + monitorId + ".jpg");
  }

  @PostMapping("/start")
  public JobDescriptor startRecording(@RequestBody RecordingDataSummary recordingData) {
    return recorderService.startRecording(recordingData);
  }

  @GetMapping("/stop/{jobId}")
  public boolean stopRecording(@PathVariable("jobId") String uuid) {
    return recorderService.stopRecording(uuid);
  }

  @GetMapping("/screenshots")
  public void takeScreenshots(HttpServletResponse response) {
    InputStream in = null;
    OutputStream out = null;
    try {
      File target = File.createTempFile("vpin-studio-screenshots", ".zip");
      target.deleteOnExit();
      if (target.exists() && !target.delete()) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete existing screenshots archive.");
      }
      LOG.info("Created temporary screenshot archive {}", target.getAbsolutePath());
      screenshotService.takeScreenshots(target);

      in = new FileInputStream(target);
      out = response.getOutputStream();
      IOUtils.copy(in, out);
      response.flushBuffer();

      LOG.info("Finished exporting screenshots.");
    }
    catch (IOException ex) {
      LOG.info("Error writing screenshots: " + ex.getLocalizedMessage(), ex);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IOError writing screenshots file to output stream");
    }
    finally {
      try {
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      }
      catch (IOException e) {
        LOG.error("Error closing streams: " + e.getMessage(), e);
      }
    }
  }

  @GetMapping("/screenshot")
  public String takeScreenshot() {
    return screenshotService.screenshot();
  }

  @GetMapping("/screenshot/{uuid}")
  public void getScreenshot(@PathVariable("uuid") String uuid, HttpServletResponse response) {
    InputStream in = null;
    OutputStream out = null;
    try {
      LOG.info("Creating summary screenshot...");
      out = response.getOutputStream();
      File screenshotFile = screenshotService.getScreenshotFile(uuid);
      if (screenshotFile.exists()) {
        in = new FileInputStream(screenshotFile);
      }
      else {
        in = screenshotService.takeScreenshot();
      }

      IOUtils.copy(in, out);
      LOG.info("Finished exporting summary screenshot.");
    }
    catch (Exception ex) {
      LOG.info("Error writing summary screenshot: {}", ex.getMessage());
    }
    finally {
      try {
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      }
      catch (IOException e) {
        LOG.error("Error closing streams: " + e.getMessage(), e);
      }
    }
  }
}
