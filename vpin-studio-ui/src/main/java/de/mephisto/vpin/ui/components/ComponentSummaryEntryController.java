package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentSummaryEntry;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

public class ComponentSummaryEntryController implements Initializable {

  @FXML
  private Label titleLabel;

  @FXML
  private Label valueLabel;

  @FXML
  private Label description;

  @FXML
  private VBox root;

  public void refresh(ComponentSummaryEntry entry) {
    String key = entry.getName();
    if (!key.endsWith(":")) {
      key += ":";
    }
    titleLabel.setText(key);

    if(entry.getValue() != null && (entry.getValue().equalsIgnoreCase("true") || entry.getValue().equalsIgnoreCase("Yes"))) {
      valueLabel.setGraphic(WidgetFactory.createCheckIcon());
      valueLabel.setText("");
    }
    else {
      valueLabel.setText(entry.getValue());
    }

    description.setVisible(!StringUtils.isEmpty(entry.getDescription()));
    description.setText(entry.getDescription());

    if (!entry.isValid()) {
      String color = "#FF3333";
      valueLabel.setStyle("-fx-font-color: " + color + ";-fx-text-fill: " + color + ";-fx-font-weight: bold;-fx-font-size: 14px;");
    }

    if (!StringUtils.isEmpty(entry.getStatus())) {
      valueLabel.setText(entry.getStatus());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    description.visibleProperty().addListener((observableValue, aBoolean, t1) -> {
      if(!t1) {
        root.setPrefHeight(28);
        root.setMinHeight(28);
      }
    });
  }
}
