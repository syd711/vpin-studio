package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.restclient.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.JobType;
import de.mephisto.vpin.restclient.PinUPControl;
import de.mephisto.vpin.restclient.PinUPControls;
import de.mephisto.vpin.tablemanager.JobListener;
import de.mephisto.vpin.tablemanager.Menu;
import de.mephisto.vpin.tablemanager.MenuController;
import de.mephisto.vpin.tablemanager.TableManagerJobPoller;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.mephisto.vpin.tablemanager.UIDefaults.FOOTER_ANIMATION_DURATION;

public class StateMananger implements JobListener, NativeKeyListener {
  private final static Logger LOG = LoggerFactory.getLogger(StateMananger.class);

  private final MediaPlayer navPlayer;
  private final MediaPlayer enterPlayer;
  private final MediaPlayer backPlayer;

  private MenuState activeState;

  private final static StateMananger INSTANCE = new StateMananger();
  private boolean blocked;

  private int LEFT;
  private int RIGHT;
  private int ENTER;
  private int BACK;
  private MenuController menuController;

  public static StateMananger getInstance() {
    return INSTANCE;
  }

  private StateMananger() {
    TableManagerJobPoller.getInstance().addJobListener(this);
    Menu.client.invalidateArchiveCache();

    String s = Menu.class.getResource("select.mp3").toExternalForm();
    System.out.println(s);
    Media media = new Media(Menu.class.getResource("select.mp3").toExternalForm());
    navPlayer = new MediaPlayer(media);
    navPlayer.setOnEndOfMedia(() -> {
      navPlayer.stop();
      navPlayer.seek(Duration.ZERO);
    });

    media = new Media(Menu.class.getResource("enter.mp3").toExternalForm());
    enterPlayer = new MediaPlayer(media);
    enterPlayer.setOnEndOfMedia(() -> {
      enterPlayer.stop();
      enterPlayer.seek(Duration.ZERO);
    });

    media = new Media(Menu.class.getResource("back.mp3").toExternalForm());
    backPlayer = new MediaPlayer(media);
    backPlayer.setOnEndOfMedia(() -> {
      backPlayer.stop();
      backPlayer.seek(Duration.ZERO);
    });
  }

  public void setInputBlocked(boolean b, long duration) {
    this.blocked = b;
    new Thread(() -> {
      try {
        Thread.sleep(duration);
      } catch (InterruptedException e) {
        //ignore1
      }
      this.blocked = !b;
    }).start();
  }

  public void setInputBlocked(boolean unblock) {
    this.blocked = unblock;
  }

  public void init(MenuController controller) {
    menuController = controller;
    this.activeState = new MainMenuState(controller);
  }

  public void handle(int keyCode) {
    if (blocked) {
      return;
    }

    if (keyCode == LEFT) {
      navPlayer.play();
      this.activeState = activeState.left();
    }
    else if (keyCode == RIGHT) {
      navPlayer.play();
      this.activeState = activeState.right();
    }
    else if (keyCode == ENTER) {
      enterPlayer.play();
      this.activeState = activeState.enter();
      LOG.info("Entered " + this.activeState);
    }
    else if (keyCode == BACK) {
      backPlayer.play();
      this.activeState = activeState.back();
      LOG.info("Went back to " + this.activeState);
    }
  }

  public void waitForJobAndGoBack() {
    this.setInputBlocked(true);
    Platform.runLater(() -> {
      TableManagerJobPoller.getInstance().setPolling();
    });
  }

  @Override
  public void updated(JobDescriptor descriptor) {
    Platform.runLater(() -> {
      menuController.setStatus(descriptor.getStatus(), descriptor.getProgress());
    });
  }

  @Override
  public void finished(JobDescriptor descriptor) {
    LOG.info("StateManager received finish job event of " + descriptor);
    this.setInputBlocked(true, FOOTER_ANIMATION_DURATION + 100);

    Platform.runLater(() -> {
      this.activeState = activeState.back();
      if(descriptor.getJobType().equals(JobType.ARCHIVE_INSTALL) || descriptor.getJobType().equals(JobType.TABLE_BACKUP)) {
        this.activeState = this.activeState.back();
      }
      this.menuController.hideProgressbar();
    });
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
    System.out.println(nativeKeyEvent.getRawCode());
  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    Platform.runLater(() -> {
      handle(nativeKeyEvent.getRawCode());
      try {
        Thread.sleep(60);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
  }

  public void setControls(PinUPControls pinUPControls) {
    LEFT = pinUPControls.getKeyCode(PinUPControl.FUNCTION_GAME_PRIOR);
    RIGHT = pinUPControls.getKeyCode(PinUPControl.FUNCTION_GAME_NEXT);
    ENTER = pinUPControls.getKeyCode(PinUPControl.FUNCTION_GAME_START);
    BACK = pinUPControls.getKeyCode(PinUPControl.FUNCTION_EXIT);
  }
}
