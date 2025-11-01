package de.mephisto.vpin.ui.jobs;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class JobsMenuHeaderController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(JobsMenuHeaderController.class);

  @FXML
  private BorderPane root;

  @FXML
  private Hyperlink clearAll;

  private JobPoller jobPoller;

  @FXML
  private void onClearAll() {
    jobPoller.dismissAll();
    jobPoller.refreshJobsUI();
  }

  @FXML
  private void onCancelAll() {
    jobPoller.cancelAll();
    jobPoller.refreshJobsUI();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    root.managedProperty().bindBidirectional(root.visibleProperty());
  }

  public void setData(JobPoller jobPoller) {
    this.jobPoller = jobPoller;
  }

  public void setVisible(boolean b) {
    root.setVisible(b);
  }
}
