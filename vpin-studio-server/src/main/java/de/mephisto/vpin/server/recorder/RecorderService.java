package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.system.ScreenInfo;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.jobs.JobService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecorderService {
  private final static Logger LOG = LoggerFactory.getLogger(RecorderService.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private ScreenPreviewService screenPreviewService;

  @Autowired
  private JobService jobService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private SystemService systemService;

  private JobDescriptor jobDescriptor;

  public JobDescriptor startRecording(RecordingDataSummary recordingData) {
    RecorderSettings settings = preferencesService.getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);

    RecorderJob job = new RecorderJob(gameService, frontendService.getFrontendConnector(), frontendStatusService, settings, recordingData, getRecordingScreens());
    jobDescriptor = new JobDescriptor(JobType.RECORDER);
    jobDescriptor.setTitle("Screen Recorder (" + recordingData.size() + " games)");
    jobDescriptor.setJob(job);

    jobService.offer(jobDescriptor);
    LOG.info("Offered screen recorder job.");
    return jobDescriptor;
  }

  public boolean stopRecording(String uuid) {
    jobService.cancel(uuid);
    return true;
  }

  public List<RecordingScreen> getRecordingScreens() {
    List<VPinScreen> supportedRecodingScreens = frontendService.getFrontend().getSupportedRecordingScreens();
    List<FrontendPlayerDisplay> frontendPlayerDisplays = frontendService.getFrontendPlayerDisplays();

    List<RecordingScreen> recordingOptions = new ArrayList<>();
    for (VPinScreen screen : supportedRecodingScreens) {
      FrontendPlayerDisplay display = VPinScreen.valueOfScreen(frontendPlayerDisplays, screen);
      RecordingScreen recordingScreen = new RecordingScreen();
      recordingScreen.setScreen(screen);
      recordingScreen.setDisplay(display);

      recordingOptions.add(recordingScreen);
    }
    return recordingOptions;
  }

  public void refreshPreview(OutputStream out, VPinScreen screen) {
    Optional<RecordingScreen> recordingScreenOpt = getRecordingScreens().stream().filter(s -> s.getScreen().equals(screen)).findFirst();
    if (recordingScreenOpt.isPresent()) {
      RecordingScreen recordingScreen = recordingScreenOpt.get();
      screenPreviewService.capture(out, recordingScreen);
    }
  }

  public void refreshPreview(OutputStream out, int monitorId) {
    Optional<ScreenInfo> monitor = systemService.getScreenInfos().stream().filter(s -> s.getId() == monitorId).findFirst();
    if (monitor.isPresent()) {
      screenPreviewService.capture(out, monitor.get());
    }
  }

  public JobDescriptor isRecording() {
    if (jobDescriptor != null && !jobDescriptor.isFinished() && !jobDescriptor.isCancelled()) {
      return jobDescriptor;
    }
    return null;
  }
}
