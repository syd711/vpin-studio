package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.archiving.RepositoryController;
import de.mephisto.vpin.ui.archiving.RepositorySidebarController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class TablesController implements Initializable, StudioFXController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(TablesController.class);

  private TableOverviewController tableOverviewController;
  private RepositoryController repositoryController;

  @FXML
  private TabPane tabPane;

  @FXML
  private Tab tablesTab;

  @FXML
  private Tab tableRepositoryTab;

  @FXML
  private TablesSidebarController tablesSideBarController; //fxml magic! Not unused

  @FXML
  private RepositorySidebarController repositorySideBarController; //fxml magic! Not unused

  @Override
  public void onViewActivated() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));
    EventManager.getInstance().addListener(this);

    try {
      FXMLLoader loader = new FXMLLoader(TableOverviewController.class.getResource("scene-tables-overview.fxml"));
      Parent tablesRoot = loader.load();
      tableOverviewController = loader.getController();
      tableOverviewController.setRootController(this);
      tablesSideBarController.setTablesController(tableOverviewController);
      tablesTab.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("failed to load table overview: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(RepositoryController.class.getResource("scene-repository.fxml"));
      Parent repositoryRoot = loader.load();
      repositoryController = loader.getController();
      repositoryController.setRootController(this);
      tableRepositoryTab.setContent(repositoryRoot);
    } catch (IOException e) {
      LOG.error("failed to load table overview: " + e.getMessage(), e);
    }


    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      Platform.runLater(() -> {
        if (t1.intValue() == 0) {
          NavigationController.setBreadCrumb(Arrays.asList("Tables"));
          tablesSideBarController.setVisible(true);
          repositorySideBarController.setVisible(false);
          tableOverviewController.initSelection();
        }
        else {
          NavigationController.setBreadCrumb(Arrays.asList("Table Repository"));
          tablesSideBarController.setVisible(false);
          repositorySideBarController.setVisible(true);
          repositoryController.initSelection();
        }
      });
    });

    tablesSideBarController.setVisible(true);
    repositorySideBarController.setVisible(false);
  }

  public TablesSidebarController getTablesSideBarController() {
    return tablesSideBarController;
  }

  public RepositorySidebarController getRepositorySideBarController() {
    return repositorySideBarController;
  }

  public TableOverviewController getTableOverviewController() {
    return tableOverviewController;
  }

  @Override
  public void jobFinished(@NonNull JobFinishedEvent event) {
    JobType jobType = event.getJobType();
    if (jobType.equals(JobType.TABLE_BACKUP) || jobType.equals(JobType.ARCHIVE_INSTALL)) {
      Platform.runLater(() -> {
        repositoryController.doReload();
      });
    }
    else if (jobType.equals(JobType.PUP_INSTALL)) {
      Platform.runLater(() -> {
        this.tableOverviewController.onReload();
      });
    }
  }

  @Override
  public void preferencesChanged() {
    Platform.runLater(() -> {
      this.tableOverviewController.onReload();
    });
  }
}
