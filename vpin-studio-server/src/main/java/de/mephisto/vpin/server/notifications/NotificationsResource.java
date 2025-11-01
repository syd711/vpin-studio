package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.restclient.highscores.NVRamList;
import de.mephisto.vpin.server.nvrams.NVRamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "notifications")
public class NotificationsResource {

  @Autowired
  private NotificationService notificationService;

  @GetMapping("test")
  public boolean test() {
    return notificationService.testNotification();
  }
}
