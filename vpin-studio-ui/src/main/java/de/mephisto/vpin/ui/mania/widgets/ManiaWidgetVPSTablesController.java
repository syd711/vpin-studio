package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.Debouncer;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ManiaWidgetVPSTablesController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaWidgetVPSTablesController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private VBox highscoreVBox;

  @FXML
  private Pane listRoot;

  @FXML
  private BorderPane root;

  @FXML
  private StackPane viewStack;

  @FXML
  private Label titleLabel;

  @FXML
  private Label countLabel;

  @FXML
  private TextField textfieldSearch;


  private Parent loadingOverlay;
  private ManiaWidgetVPSTableRankController tableRankController;

  private final List<Predicate<VpsTable>> predicates = new ArrayList<>();
  private String selectedLetter;
  private VpsTable selectedTable;
  private final List<Pane> vpsTableItems = new ArrayList<>();

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
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay-plain.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading VPS Tables");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    bindSearchField();
  }


  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      debouncer.debounce("search", () -> {
        Platform.runLater(() -> {
          String term = textfieldSearch.getText();
          if (StringUtils.isEmpty(term)) {
            highscoreVBox.getChildren().removeAll(highscoreVBox.getChildren());
            highscoreVBox.getChildren().addAll(vpsTableItems);
          }
          else {
            highscoreVBox.getChildren().removeAll(highscoreVBox.getChildren());

            List<Pane> collect = vpsTableItems.stream().filter(vps -> ((VpsTable) vps.getUserData()).getName().toLowerCase().contains(term.toLowerCase())).collect(Collectors.toList());
            highscoreVBox.getChildren().addAll(collect);

            if (collect.size() == 1) {
              selectTable((VpsTable) collect.get(0).getUserData());
            }
          }
        });
      }, 300);
    });
  }


  public void setData(String selectedLetter, @Nullable VpsTable vpsTable) {
    this.selectedLetter = selectedLetter;
    this.selectedTable = vpsTable;

    highscoreVBox.getChildren().removeAll(highscoreVBox.getChildren());
    listRoot.setVisible(false);
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
      vpsTableItems.clear();

      List<VpsTable> tables = getTablesForLetter(selectedLetter);
      if (tables.isEmpty()) {
        Label label = new Label("No tables found for letter '" + selectedLetter + "'");
        label.setPadding(new Insets(80, 0, 0, 100));
        label.getStyleClass().add("preference-description");
      }
      else {
        try {
          for (VpsTable table : tables) {
            FXMLLoader loader = new FXMLLoader(ManiaWidgetVPSTableController.class.getResource("mania-widget-vps-table.fxml"));
            BorderPane row = loader.load();
            row.setUserData(table);
            row.getStyleClass().add("vps-table-button");
            row.setPrefWidth(root.getPrefWidth() - 40);
            ManiaWidgetVPSTableController controller = loader.getController();
            controller.setTablesController(this);
            controller.setData(table);

            if (table.equals(vpsTable) && !row.getStyleClass().contains("vps-table-button-selected")) {
              row.getStyleClass().add("vps-table-button-selected");
            }
            vpsTableItems.add(row);
          }
        }
        catch (IOException e) {
          LOG.error("Failed to load table item: " + e.getMessage(), e);
        }
      }

      Platform.runLater(() -> {
        listRoot.setVisible(true);
        countLabel.setText(vpsTableItems.size() + " tables");
        if (vpsTableItems.isEmpty()) {
          Label label = new Label("No tables found for letter '" + selectedLetter + "'");
          label.setPadding(new Insets(80, 0, 0, 100));
          label.getStyleClass().add("preference-description");
        }
        else {
          highscoreVBox.getChildren().addAll(vpsTableItems);
        }

        for (Predicate<VpsTable> predicate : predicates) {
          predicate.test(vpsTable);
        }
        predicates.clear();
        viewStack.getChildren().remove(loadingOverlay);
      });
    }).start();
  }

  private List<VpsTable> getTablesForLetter(String l) {
    String term = this.textfieldSearch.getText();
    List<VpsTable> result = new ArrayList<>();
    List<VpsTable> tables = Studio.client.getVpsService().getTables();
    Collections.sort(tables, (o1, o2) -> {
      if (o1 == null || o2 == null || o1.getName().isEmpty() || o2.getName().isEmpty()) {
        return -1;
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
    this.selectedTable = vpsTable;
    ObservableList<Node> children = highscoreVBox.getChildren();
    for (Node child : children) {
      child.getStyleClass().remove("vps-table-button-selected");
      if (vpsTable != null && child.getUserData().equals(vpsTable) && !child.getStyleClass().contains("vps-table-button-selected")) {
        child.getStyleClass().add("vps-table-button-selected");
      }
    }
    tableRankController.setData(vpsTable);
  }
}