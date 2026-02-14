package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class VpsTableContainer extends VBox {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final static int TITLE_WIDTH = 100;

  public VpsTableContainer(VpsTable item) {
    this(item, "");
  }

  public VpsTableContainer(@NonNull VpsTable item, String customStyle) {
    super(3);

    String name = item.getName();
    if (name.length() > 40) {
      name = name.substring(0, 39) + "...";
    }

    Label title = new Label(name);
    title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;" + customStyle);
    this.getChildren().add(title);

    HBox row = new HBox(6);
    Label titleLabel = new Label("Year:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;" + customStyle);
    Label valueLabel = new Label(String.valueOf(item.getYear()));
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;" + customStyle);
    row.getChildren().addAll(titleLabel, valueLabel);

    this.getChildren().add(row);

    row = new HBox(6);
    titleLabel = new Label("Manufacturer:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;" + customStyle);
    valueLabel = new Label(item.getManufacturer());
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;" + customStyle);
    row.getChildren().addAll(titleLabel, valueLabel);
    this.getChildren().add(row);

    row = new HBox(6);
    titleLabel = new Label("Type:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;" + customStyle);
    valueLabel = new Label(item.getType());
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;" + customStyle);
    row.getChildren().addAll(titleLabel, valueLabel);
    this.getChildren().add(row);

    row = new HBox(6);


//    titleLabel = new Label("");
//    titleLabel.setPrefWidth(TITLE_WIDTH);
//    row.getChildren().addAll(titleLabel);


    this.getChildren().add(row);

    row.setPadding(new Insets(3, 0, 6, 0));
  }
}
