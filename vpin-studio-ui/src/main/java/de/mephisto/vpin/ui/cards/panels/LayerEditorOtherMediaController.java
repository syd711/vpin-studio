package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.ui.tables.TableDialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;

public class LayerEditorOtherMediaController extends LayerEditorBaseController {

  @FXML
  private CheckBox otherMediaKeepARCheckBox;

  @FXML
  private ComboBox<VPinScreen> otherMediaScreensComboBox;

  @FXML
  private VBox missingMediaBox;

  @FXML
  private Label missingMediaLabel;

  private Optional<GameRepresentation> game = Optional.empty();

  @FXML
  private void onAddMedia() {
    VPinScreen screen = otherMediaScreensComboBox.getValue();
    if (game.isPresent() && screen != null) {
      TableDialogs.openTableAssetsDialog(null, game.get(), screen);
      refreshMissingMedia();
    }
  }

  /**
   * Shows the empty state when the selected table has no media for the chosen screen
   */
  private void refreshMissingMedia() {
    missingMediaBox.setVisible(false);
    VPinScreen screen = otherMediaScreensComboBox.getValue();
    if (game.isEmpty() || screen == null) {
      return;
    }
    int gameId = game.get().getId();
    JFXFuture.supplyAsync(() -> {
          FrontendMediaRepresentation media = client.getFrontendService().getFrontendMedia(gameId);
          return media == null || media.getDefaultMediaItem(screen) == null;
        })
        .thenAcceptLater(missing -> {
          // ignore outdated answers
          if (game.isPresent() && game.get().getId() == gameId && screen.equals(otherMediaScreensComboBox.getValue())) {
            missingMediaLabel.setText(Messages.get("cards.designer.no_media_for_table", screen.toString()));
            missingMediaBox.setVisible(missing);
          }
        });
  }

  public void setTemplate(CardTemplate cardTemplate, int cardWidth, int cardHeight, Optional<GameRepresentation> game) {
    setIconVisibility(cardTemplate.isRenderOtherMedia());
    setIconLock(cardTemplate.isLockOtherMedia(), cardTemplate.isTemplate());

    positionController.setTemplate("otherMedia", cardTemplate, cardWidth, cardHeight, true);

    otherMediaKeepARCheckBox.setSelected(cardTemplate.isOtherMediaKeepAspectRatio());
    otherMediaScreensComboBox.getSelectionModel().select(cardTemplate.getOtherMediaScreen());

    this.game = game;
    refreshMissingMedia();
  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderOtherMedia");
    bindLockIcon(templateBeanBinder, "lockOtherMedia");

    positionController.initBindings("otherMedia", templateBeanBinder, true);

    templateBeanBinder.bindCheckbox(otherMediaKeepARCheckBox, "otherMediaKeepAspectRatio");

    Frontend frontend = client.getFrontendService().getFrontend();
    List<VPinScreen> VPinScreens = new ArrayList<>(frontend.getSupportedScreens());
    VPinScreens.remove(VPinScreen.Audio);
    VPinScreens.remove(VPinScreen.AudioLaunch);
    //VPinScreens.remove(VPinScreen.Other2);
    //VPinScreens.remove(VPinScreen.GameInfo);
    //VPinScreens.remove(VPinScreen.GameHelp);
    //VPinScreens.remove(VPinScreen.Topper);
    //VPinScreens.remove(VPinScreen.DMD);
    //VPinScreens.remove(VPinScreen.Logo);
    VPinScreens.remove(VPinScreen.Wheel);
    VPinScreens.remove(VPinScreen.Menu);
    VPinScreens.remove(VPinScreen.BackGlass);
    VPinScreens.remove(VPinScreen.PlayField);
    VPinScreens.remove(VPinScreen.Loading);
    otherMediaScreensComboBox.setItems(FXCollections.observableList(VPinScreens));

    templateBeanBinder.bindComboBox(otherMediaScreensComboBox, "otherMediaScreen");

    missingMediaBox.managedProperty().bind(missingMediaBox.visibleProperty());
    missingMediaBox.setVisible(false);
    otherMediaScreensComboBox.valueProperty().addListener((obs, o, n) -> refreshMissingMedia());
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
    positionController.bindDragBox(dragBox);
  }

  @Override
  public void unbindDragBox(PositionResizer dragBox) {
    positionController.unbindDragBox(dragBox);
  }
}
