package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.restclient.alx.AlxTileEntry;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuCustomTileEntryController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private Label titleLabel;

  @FXML
  private Label descriptionLabel;

  @FXML
  private Label valueLabel;

  public void refresh(@NonNull AlxTileEntry entry) {
    titleLabel.setText(entry.getTitle());
    valueLabel.setText(entry.getValue());
    descriptionLabel.setText(entry.getDescription());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }
}
