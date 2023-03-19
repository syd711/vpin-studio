package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.tablemanager.MenuController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


public class ArchiveOptionsMenuState extends MenuState {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  private final MenuState parentState;
  private final MenuController menuController;
  private final List<String> options = Arrays.asList("Remove From All Playlists: YES", "Remove From All Playlists: NO");
  private int index = 0;

  public ArchiveOptionsMenuState(MenuState parentState, MenuController menuController) {
    this.parentState = parentState;
    this.menuController = menuController;
    this.menuController.enterArchiveOptionsSelection();

    this.menuController.setNameLabelText(options.get(0));
  }

  @Override
  MenuState left() {
    if (index > 0) {
      index--;
    }
    else {
      index = options.size() - 1;
    }

    this.menuController.setNameLabelText(options.get(index));
    return this;
  }

  @Override
  MenuState right() {
    if (index == options.size() - 1) {
      index = 0;
    }
    else {
      index++;
    }

    this.menuController.setNameLabelText(options.get(index));
    return this;
  }

  @Override
  MenuState enter() {
    menuController.showProgressbar();
    return new ArchivingMenuState(this, index, menuController);
  }

  @Override
  MenuState back() {
    menuController.leaveArchiveSubSelection();
    return parentState.back();
  }
}
