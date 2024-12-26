package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.monitor.MonitoringSettings;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.util.RequestUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

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
  private PreferencesService preferencesService;

  @GetMapping("/screens")
  public List<RecordingScreen> getRecordingScreens() {
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
      MonitoringSettings monitoringSettings = preferencesService.getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);

      File target = new File("vpin-studio-screenshots.zip");
      target.deleteOnExit();
      if (target.exists() && !target.delete()) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete existing screenshots archive.");
      }

      List<File> screenshotFiles = takeFrontendScreenshots(monitoringSettings);

      FileOutputStream fos = new FileOutputStream(target);
      ZipOutputStream zipOut = new ZipOutputStream(fos);

      for (File screenshotFile : screenshotFiles) {
        ZipUtil.zipFile(screenshotFile, screenshotFile.getName(), zipOut);
      }
      zipOut.close();
      fos.close();

      in = new FileInputStream(target);
      out = response.getOutputStream();
      IOUtils.copy(in, out);
      response.flushBuffer();

      for (File screenshotFile : screenshotFiles) {
        if(!screenshotFile.delete()) {
          LOG.warn("Failed to delete temporary screenshot file " + screenshotFile.getAbsolutePath());
        }
      }
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

  private List<File> takeFrontendScreenshots(MonitoringSettings monitoringSettings) {
    List<File> screenshotFiles = new ArrayList<>();
    List<VPinScreen> disabledScreens = monitoringSettings.getDisabledScreens();
    List<RecordingScreen> supportedRecordingScreens = recorderService.getRecordingScreens();
    for (RecordingScreen recordingScreen : supportedRecordingScreens) {
      try {
        VPinScreen screen = recordingScreen.getScreen();
        if (!disabledScreens.contains(screen)) {
          File file = File.createTempFile("screenshot", ".jpg");
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          recorderService.refreshPreview(out, screen);
          out.close();

          FileOutputStream fileOutputStream = new FileOutputStream(file);
          fileOutputStream.write(out.toByteArray());
          fileOutputStream.close();

          String name = "screenshot-" + screen.getSegment() + ".jpg";
          File target = new File(file.getParentFile(), name);
          if (target.exists() && !target.delete()) {
            throw new Exception("Failed to delete temporary screenshot file " + target.getAbsolutePath());
          }

          ImageUtil.drawTimestamp(file);
          file.renameTo(target);

          screenshotFiles.add(target);
          LOG.info("Written screenshot " + target.getAbsolutePath());
        }
      }
      catch (Exception e) {
        LOG.error("Error writing screenshot: {}", e.getMessage(), e);
      }
    }
    return screenshotFiles;
  }
}
