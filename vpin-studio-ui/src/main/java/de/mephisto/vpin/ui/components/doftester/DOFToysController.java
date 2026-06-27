package de.mephisto.vpin.ui.components.doftester;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.doftester.DOFTesterSettings;
import de.mephisto.vpin.restclient.doftester.ToySummary;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.PreferencesController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DOFToysController implements Initializable {

  public static Debouncer debouncer = new Debouncer();

  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private DOFTesterController dofTesterController;

  @FXML
  private VBox toyListPane;


  @FXML
  private Spinner<Integer> durationSpinner;

  public DOFToysController() {
  }

  @FXML
  private void onDOFSettings() {
    PreferencesController.open("dof");
  }

  public void setParentController(DOFTesterController dofTesterController) {
    this.dofTesterController = dofTesterController;
  }

  public void selectTable(Optional<GameRepresentation> game) {
    toyListPane.getChildren().removeAll(toyListPane.getChildren());

    if (game.isPresent()) {
      GameRepresentation gameRepresentation = game.get();
      ToySummary toys = client.getDofTesterService().getToys(gameRepresentation.getId());
      for (String toy : toys.getToys()) {
        try {
          FXMLLoader loader = new FXMLLoader(ToyContainerController.class.getResource("toy-container.fxml"));
          Pane root = loader.load();
          root.getStyleClass().add("dropin-menu-item");
          ToyContainerController controller = loader.getController();
          controller.setData(game, toy);

          toyListPane.getChildren().add(root);
        }
        catch (IOException e) {
          LOG.error("Failed to load toy container: " + e.getMessage(), e);
        }
      }
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    DOFTesterSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.DOF_TESTER_SETTINGS, DOFTesterSettings.class);

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999999, 200);
    durationSpinner.setValueFactory(factory);
    factory.valueProperty().set(settings.getTestDuration());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("testDuration", () -> {
        settings.setTestDuration(t1);
        client.getPreferenceService().setJsonPreference(settings);
      }, 100);
    });
  }
}
