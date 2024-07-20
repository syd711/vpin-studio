package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.mania.widgets.ManiaWidgetVPSTableRankController;
import de.mephisto.vpin.ui.mania.widgets.ManiaWidgetVPSTablesController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class TabManiaTablesController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TabManiaTablesController.class);

  @FXML
  private BorderPane widgetSidePanel;

  @FXML
  private BorderPane widgetRight;

  @FXML
  private ToolBar toolbar;

  private String selectedLetter = null;

  private ManiaWidgetVPSTablesController tablesController;
  private ManiaWidgetVPSTableRankController tableRankController;

  @Override
  public void onViewActivated(@Nullable NavigationOptions options) {
    tablesController.setData(selectedLetter);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ToggleGroup group = new ToggleGroup();
    List<String> letters = getLetters();
    this.selectedLetter = letters.get(0);

    for (String letter : letters) {
      ToggleButton b = new ToggleButton(letter);
      b.getStyleClass().add("default-text");
      b.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          if(newValue) {
            selectedLetter = letter;
            tablesController.setData(letter);
          }
        }
      });
      b.setToggleGroup(group);
      toolbar.getItems().add(b);
    }


    try {
      FXMLLoader loader = new FXMLLoader(ManiaWidgetVPSTablesController.class.getResource("mania-widget-vps-tables.fxml"));
      BorderPane root = loader.load();
      tablesController = loader.getController();
      tablesController.setData(selectedLetter);
      root.setMaxHeight(Double.MAX_VALUE);
      widgetSidePanel.setLeft(root);
    }
    catch (IOException e) {
      LOG.error("Failed to load mania-widget-vps-tables.fxml: " + e.getMessage(), e);
    }


    try {
      FXMLLoader loader = new FXMLLoader(ManiaWidgetVPSTableRankController.class.getResource("mania-widget-vps-table-rank.fxml"));
      BorderPane rankPane = loader.load();
      tableRankController = loader.getController();
      rankPane.setMaxWidth(Double.MAX_VALUE);
      rankPane.setMaxHeight(Double.MAX_VALUE);
      widgetRight.setCenter(rankPane);
    }
    catch (IOException e) {
      LOG.error("Failed to load mania-widget-vps-table-rank.fxml: " + e.getMessage(), e);
    }

    tablesController.setTableRankController(tableRankController);

    ((ToggleButton)toolbar.getItems().get(0)).setSelected(true);
    onViewActivated(null);
  }

  private List<String> getLetters() {
    List<String> letters = new ArrayList<>();
    List<VpsTable> tables = Studio.client.getVpsService().getTables();
    Collections.sort(tables, (o1, o2) -> {
      if (o1.getName().isEmpty() || o2.getName().isEmpty()) {
        return 0;
      }
      return o1.getName().compareTo(o2.getName());
    });

    for (VpsTable table : tables) {
      if (table.getName().trim().isEmpty()) {
        continue;
      }

      String letter = table.getName().trim().substring(0, 1);
      if (!letters.contains(letter)) {
        letters.add(letter);
      }
    }

    Collections.sort(letters);
    return letters;
  }
}
