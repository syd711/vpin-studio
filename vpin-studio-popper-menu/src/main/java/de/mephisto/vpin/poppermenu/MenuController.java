package de.mephisto.vpin.poppermenu;

import de.mephisto.vpin.commons.fx.widgets.WidgetLatestScoresController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MenuController.class);

  @FXML
  private Node installPanel;

  @FXML
  private Node uninstallPanel;

  @FXML
  private Node baseSelector;

  @FXML
  private HBox gameRow;

  private boolean installToggle = true;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void toggleInstall() {
    if(installToggle) {
      baseSelector.setStyle("-fx-background-color: #33CC00;");
      TransitionUtil.createOutFader(installPanel).play();
      TransitionUtil.createInFader(uninstallPanel).play();
    }
    else {
      baseSelector.setStyle("-fx-background-color: #FF3333;");
      TransitionUtil.createOutFader(uninstallPanel).play();
      TransitionUtil.createInFader(installPanel).play();
    }
    installToggle = !installToggle;
  }

  public boolean isInstallSelected() {
    return this.installToggle;
  }

  public void enterInstall() {
    TransitionUtil.createOutFader(uninstallPanel).play();
    TransitionUtil.createOutFader(installPanel).play();
    TransitionUtil.createInFader(gameRow).play();
    List<VpaDescriptorRepresentation> vpaDescriptors = MenuMain.client.getVpaDescriptors();

    try {
      for (VpaDescriptorRepresentation vpaDescriptor : vpaDescriptors) {
//        FXMLLoader loader = new FXMLLoader(ArchiveItemController.class.getResource("menu-archive-item.fxml"));
//        BorderPane root = loader.load();
//        gameRow.getChildren().add(root);
//
//        ArchiveItemController controller = loader.getController();
//        controller.setData(vpaDescriptor);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(440);
        imageView.setFitHeight(440);
        String thumbnail = vpaDescriptor.getManifest().getIcon();
        if (thumbnail == null) {
          Image wheel = new Image(MenuMain.class.getResourceAsStream("avatar-blank.png"));
          imageView.setImage(wheel);
          gameRow.getChildren().add(imageView);
        }
        else {
          byte[] decode = Base64.getDecoder().decode(thumbnail);
          Image wheel = new Image(new ByteArrayInputStream(decode));
          imageView.setImage(wheel);
          gameRow.getChildren().add(imageView);
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to load item: " + e.getMessage());
    }

  }

  public void enterMainWithInstall() {
    TransitionUtil.createOutFader(gameRow).play();
    TransitionUtil.createOutFader(uninstallPanel).play();
    TransitionUtil.createInFader(installPanel).play();
  }

  public void scrollGameBarRight() {
    TransitionUtil.createTranslateByXTransition(gameRow, 60, -440).play();
  }

  public void scrollGameBarLeft() {
    TransitionUtil.createTranslateByXTransition(gameRow, 60, 440).play();
  }
}
