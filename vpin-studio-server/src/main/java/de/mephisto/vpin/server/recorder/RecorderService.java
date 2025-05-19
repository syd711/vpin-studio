package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.restclient.recorder.*;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.server.dmd.DMDPositionService;
import de.mephisto.vpin.server.fp.FPService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.VPinScreenService;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.jobs.JobService;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.VPXService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class RecorderService {
  private final static Logger LOG = LoggerFactory.getLogger(RecorderService.class);

  private final static List<VPinScreen> supportedRecodingScreens = Arrays.asList(VPinScreen.PlayField, VPinScreen.BackGlass, 
  VPinScreen.DMD, VPinScreen.Menu, VPinScreen.Topper);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private VPinScreenService screenService;

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

  @Autowired
  private VPXService vpxService;

  @Autowired
  private FPService fpService;

  @Autowired
  private DMDPositionService dmdPositionService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  private JobDescriptor jobDescriptor;

  public JobDescriptor startRecording(RecordingDataSummary recordingData) {
    RecorderSettings settings = preferencesService.getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);

    Job job = null;
    if (settings.getRecordingMode() == null || settings.getRecordingMode().equals(RecordingMode.emulator)) {
      job = new EmulatorRecorderJob(gameLifecycleService, dmdPositionService, gameService, vpxService, fpService, frontendService.getFrontendConnector(), frontendStatusService, settings, recordingData, getRecordingScreens());
    }
    else {
      job = new FrontendRecorderJob(gameLifecycleService, dmdPositionService, gameService, frontendService.getFrontendConnector(), frontendStatusService, settings, recordingData, getRecordingScreens());
    }

    jobDescriptor = new JobDescriptor(JobType.RECORDER);
    jobDescriptor.setTitle("Screen Recorder (" + recordingData.size() + " games)");
    jobDescriptor.setUserData(0);
    jobDescriptor.setJob(job);
    jobService.offer(jobDescriptor);
    LOG.info("Offered screen recorder job.");
    return jobDescriptor;
  }

  public JobDescriptor startInGameRecording() {
    GameStatus gameStatus = frontendStatusService.getGameStatus();
    int gameId = gameStatus.getGameId();

    RecorderSettings settings = preferencesService.getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);

    RecordingDataSummary recordingData = new RecordingDataSummary();
    RecordingData recordingDataEntry = new RecordingData();
    recordingData.add(recordingDataEntry);
    for (VPinScreen screen : VPinScreen.values()) {
      RecordingScreenOptions recordingScreenOption = settings.getRecordingScreenOption(screen);
      if (recordingScreenOption == null) {
        LOG.info("Skipped recording for {}, because screen is not enabled.", screen.name());
        continue;
      }
      if (recordingScreenOption.isInGameRecording() && recordingScreenOption.isEnabled()) {
        recordingDataEntry.addScreen(screen);
      }
      else {
        LOG.info("Skipped recording for {}, because screen is not enabled for in-game recording.", screen.name());
      }
    }

    recordingDataEntry.setGameId(gameId);

    List<FrontendPlayerDisplay> recordingScreens = getRecordingScreens();
    for (FrontendPlayerDisplay recordingScreen : recordingScreens) {
      RecordingScreenOptions option = settings.getRecordingScreenOption(recordingScreen.getScreen());
      if (option.isEnabled() && option.isInGameRecording()) {
        recordingDataEntry.addScreen(recordingScreen.getScreen());
      }
    }

    if (!recordingDataEntry.getScreens().isEmpty()) {
      NotificationSettings notificationSettings = preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
      FrontendRecorderJob job = new InGameRecorderJob(gameLifecycleService, dmdPositionService, notificationService, gameService, frontendService.getFrontendConnector(), frontendStatusService, settings, notificationSettings, recordingData, getRecordingScreens());
      jobDescriptor = new JobDescriptor(JobType.RECORDER);
      jobDescriptor.setTitle("In-Game Screen Recorder");
      jobDescriptor.setJob(job);

      jobService.offer(jobDescriptor);
      LOG.info("Offered in-game screen recorder job.");
      return jobDescriptor;
    }

    jobDescriptor.setProgress(1);
    jobDescriptor.setStatus("Cancelled, no active screens");
    return jobDescriptor;
  }

  public boolean stopRecording(String uuid) {
    jobService.cancel(uuid);
    return true;
  }

  public List<FrontendPlayerDisplay> getRecordingScreens() {
    List<FrontendPlayerDisplay> result = new ArrayList<>();
    for (VPinScreen screen : supportedRecodingScreens) {
      FrontendPlayerDisplay display = screenService.getRecordingScreenDisplay(screen);
      // recording screen may not be among the effective displays
      if (display != null) {
        result.add(display);
      }
    }
    return result;
  }

  public void refreshPreview(OutputStream out, VPinScreen screen) {
    Optional<FrontendPlayerDisplay> recordingScreenOpt = getRecordingScreens().stream().filter(s -> s.getScreen().equals(screen)).findFirst();
    if (recordingScreenOpt.isPresent()) {
      FrontendPlayerDisplay recordingScreen = recordingScreenOpt.get();
      screenPreviewService.capture(out, recordingScreen);
    }
  }

  public void refreshPreview(OutputStream out, int monitorId) {
    Optional<MonitorInfo> monitor = systemService.getMonitorInfos().stream().filter(s -> s.getId() == monitorId).findFirst();
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
