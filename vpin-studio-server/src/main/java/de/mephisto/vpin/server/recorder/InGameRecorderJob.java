package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.commons.fx.notifications.Notification;
import de.mephisto.vpin.commons.fx.notifications.NotificationFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.restclient.recorder.*;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.games.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import de.mephisto.vpin.server.util.ServerMessages;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

public class InGameRecorderJob extends FrontendRecorderJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(InGameRecorderJob.class);

  private final NotificationSettings notificationSettings;
  private final boolean showNotifications;

  public InGameRecorderJob(RecorderService recorderService, NotificationSettings notificationSettings,  
                           RecorderSettings settings, RecordingDataSummary recordingDataSummary, List<FrontendPlayerDisplay> recordingScreens) {
    super(recorderService, settings, recordingDataSummary, recordingScreens);
    this.notificationSettings = notificationSettings;
    this.showNotifications = !recorderService.isEmulatorInFullscreenMode();
  }

  @Override
  public void execute(JobDescriptor jobDescriptor) {
    FrontendConnector frontend = recorderService.getFrontendConnector();

    LOG.info("***************************** In-Game Recording Log ******************************************************");
    for (RecordingData data : recordingDataSummary.getRecordingData()) {
      Game game = recorderService.getGame(data);

      LOG.info("************************ \"{}\" ************************", game.getGameDisplayName());
      try {
        if (showStartNotification(jobDescriptor, data)) {
          return;
        }

        updateSingleProgress(jobDescriptor, recordingDataSummary, 10);
        if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
          break;
        }

        jobDescriptor.setGameId(game.getId());
        jobDescriptor.setStatus(ServerMessages.get("recorder.status.launching_frontend", resolveLocale()));

        try {
          jobDescriptor.setStatus(ServerMessages.get("recorder.status.recording", resolveLocale(), game.getGameDisplayName()));

          //create the game recorder which includes all screens
          gameRecorder = new GameRecorder(frontend, game, recorderSettings, data, jobDescriptor, getRecordingScreensForGame(game));
          gameRecorder.startRecording();
        }
        finally {
          frontend.endGameRecording(game);
        }

        updateSingleProgress(jobDescriptor, recordingDataSummary, 90);
        LOG.info("Recording for \"{}\" finished.", game.getGameDisplayName());
        jobDescriptor.setTasksExecuted(jobDescriptor.getTasksExecuted() + 1);
        double progress = jobDescriptor.getTasksExecuted() * 100d / recordingDataSummary.size() / 100d;
        jobDescriptor.setProgress(progress);

        showEndNotification(jobDescriptor, data);
      }
      catch (Exception e) {
        LOG.error("Game recording failed: {}", e.getMessage(), e);
      }
      finally {
        LOG.info("Recordings for {} games finished.", recordingDataSummary.size());
        jobDescriptor.setProgress(1);
        jobDescriptor.setGameId(-1);
        if (gameRecorder != null) {
          gameRecorder.finalizeRecordings();
        }
        recorderService.notifyGameAssetsChanged(game.getId(), AssetType.FRONTEND_MEDIA, null);
      }
    }

    LOG.info("***************************** /In-Game Recording Log *****************************************************");
  }

  private void showEndNotification(JobDescriptor jobDescriptor, RecordingData data) {
    if (showNotifications && notificationSettings.isRecordingEndNotification()) {
      Locale locale = ServerMessages.resolveLocalLocale();
      Notification notification = NotificationFactory.createNotification(null, ServerMessages.get("notification.recording.title", locale), ServerMessages.get("notification.recording.end", locale), ServerMessages.get("notification.recording.end_message", locale));
      recorderService.showNotificationNow(notification);
    }
  }

  private boolean showStartNotification(JobDescriptor jobDescriptor, RecordingData data) throws InterruptedException {
    if (showNotifications && notificationSettings.isRecordingStartNotification()) {
      int seconds = notificationSettings.getDurationSec();

      int wait = 0;
      for (VPinScreen screen : data.getScreens()) {
        RecordingScreenOptions option = recorderSettings.getRecordingScreenOption(screen);
        if (option.getInitialDelay() > 0 && option.getInitialDelay() < wait) {
          wait = option.getInitialDelay();
        }
      }

      seconds = seconds + wait;
      Locale startLocale = ServerMessages.resolveLocalLocale();
      Notification notification = NotificationFactory.createNotification(null, ServerMessages.get("notification.recording.title", startLocale), ServerMessages.get("notification.recording.start", startLocale), ServerMessages.get("notification.recording.start_message", startLocale, seconds));
      if (seconds > 0) {
        notification.setDurationSec(seconds - 1);
      }

      recorderService.showNotificationNow(notification);

      while (seconds > 0) {
        Thread.sleep(1000);
        if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
          return true;
        }
        LOG.info("Recording starting in {} seconds.", seconds);
        seconds--;
      }
      Thread.sleep(300);
    }
    return false;
  }

  private Locale resolveLocale() {
    try {
      ServletRequestAttributes attrs =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attrs != null) {
        String lang = attrs.getRequest().getHeader("Accept-Language");
        return ServerMessages.parseLocale(lang);
      }
    }
    catch (Exception ignored) {}
    return Locale.ENGLISH;
  }

}
