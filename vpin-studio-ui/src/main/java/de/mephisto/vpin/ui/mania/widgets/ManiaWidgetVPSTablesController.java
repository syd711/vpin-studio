package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.ui.Studio;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class ManiaWidgetVPSTablesController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaWidgetVPSTablesController.class);

  @FXML
  private VBox highscoreVBox;

  @FXML
  private BorderPane root;

  @FXML
  private StackPane viewStack;

  @FXML
  private Label titleLabel;

  @FXML
  private Label countLabel;

  private Parent loadingOverlay;
  private ManiaWidgetVPSTableRankController tableRankController;

  private final List<Predicate<VpsTable>> predicates = new ArrayList<>();
  private String selectedLetter;

  // Add a public no-args constructor
  public ManiaWidgetVPSTablesController() {
  }

  @FXML
  private void onReload() {
    if (this.selectedLetter != null) {
      setData(selectedLetter, null);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading VPS Tables");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }


  public void setData(String selectedLetter, @Nullable VpsTable vpsTable) {
    this.selectedLetter = selectedLetter;
    countLabel.setText("");
    titleLabel.setText("Tables for \"" + selectedLetter + "\"");
    if (!viewStack.getChildren().contains(loadingOverlay)) {
      viewStack.getChildren().add(loadingOverlay);
    }

    this.predicates.add(new Predicate<VpsTable>() {
      @Override
      public boolean test(VpsTable vpsTable) {
        Platform.runLater(() -> {
          tableRankController.setData(vpsTable);
        });
        return true;
      }
    });


    new Thread(() -> {
      List<VpsTable> tables = getTablesForLetter(selectedLetter);
      Platform.runLater(() -> {
        highscoreVBox.getChildren().removeAll(highscoreVBox.getChildren());

        try {
          countLabel.setText(tables.size() + " tables");
          if (tables.isEmpty()) {
            Label label = new Label("No tables found for letter '" + selectedLetter + "'");
            label.setPadding(new Insets(80, 0, 0, 100));
            label.getStyleClass().add("preference-description");
            highscoreVBox.getChildren().add(label);
          }
          else {
            for (VpsTable table : tables) {
              FXMLLoader loader = new FXMLLoader(ManiaWidgetVPSTableController.class.getResource("mania-widget-vps-table.fxml"));
              BorderPane row = loader.load();
              row.setUserData(table);
              row.getStyleClass().add("vps-table-button");
              row.setPrefWidth(root.getPrefWidth() - 40);
              ManiaWidgetVPSTableController controller = loader.getController();
              controller.setTablesController(this);
              controller.setData(table);

              if(table.equals(vpsTable) && !row.getStyleClass().contains("vps-table-button-selected")) {
                row.getStyleClass().add("vps-table-button-selected");
              }

              highscoreVBox.getChildren().add(row);
            }
          }

          for (Predicate<VpsTable> predicate : predicates) {
            predicate.test(vpsTable);
          }
          predicates.clear();

        }
        catch (IOException e) {
          LOG.error("Failed to create widget: " + e.getMessage(), e);
        }

        viewStack.getChildren().remove(loadingOverlay);
      });
    }).start();
  }

  private List<VpsTable> getTablesForLetter(String l) {
    List<VpsTable> result = new ArrayList<>();
    List<VpsTable> tables = Studio.client.getVpsService().getTables();
    Collections.sort(tables, (o1, o2) -> {
      if (o1 == null || o2 == null || o1.getName().isEmpty() || o2.getName().isEmpty()) {
        return 0;
      }
      return o1.getName().compareTo(o2.getName());
    });

    for (VpsTable table : tables) {
      if (table == null) {
        continue;
      }

      if (table.getName().trim().isEmpty()) {
        continue;
      }

      if (table.getTableFiles() == null || table.getTableFiles().isEmpty()) {
        continue;
      }

      String letter = table.getName().trim().substring(0, 1);
      if (letter.equals(l)) {
        result.add(table);
      }
    }

    return result;
  }

  public void setTableRankController(ManiaWidgetVPSTableRankController tableRankController) {
    this.tableRankController = tableRankController;
  }

  public void selectTable(VpsTable vpsTable) {
    ObservableList<Node> children = highscoreVBox.getChildren();
    for (Node child : children) {
      child.getStyleClass().remove("vps-table-button-selected");
      if(vpsTable != null && child.getUserData().equals(vpsTable) && !child.getStyleClass().contains("vps-table-button-selected")) {
        child.getStyleClass().add("vps-table-button-selected");
      }
    }
    tableRankController.setData(vpsTable);
  }
}