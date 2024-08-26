package de.mephisto.vpin.ui;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

public class WaitOverlay {
  private final static Logger LOG = LoggerFactory.getLogger(WaitOverlay.class);

  private StackPane pane;
  
  private TableView<?> tableView;

  private Parent overlay;

  private WaitOverlayController controller;

  public WaitOverlay(StackPane pane, TableView<?> tableView, String message) {
    this.pane = pane;
    this.tableView = tableView;

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      this.overlay = loader.load();
      this.controller = loader.getController();
      controller.setLoadingMessage(message);
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }

  public void show() {
    tableView.setVisible(false);
    if (!pane.getChildren().contains(overlay)) {
      pane.getChildren().add(overlay);
    }
  }

  public void hide() {
    pane.getChildren().remove(overlay);
    tableView.setVisible(true);
  }

  public void toggle(boolean b) {
    if (b) {
      show();
    }
    else {
      hide();
    }
  }

  public void setBusy(String msg, boolean b) {
    controller.setLoadingMessage(msg);
    toggle(b);
  }

}
