package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.stage;

import java.util.*;

public class LayerEditorScoresController extends LayerEditorBaseController {

  @FXML
  private Label scoreFontLabel;
  @FXML
  private ColorPicker fontColorSelector;
  @FXML
  private ColorPicker friendsFontColorSelector;
  @FXML
  private Spinner<Integer> rowSeparatorSpinner;
  @FXML
  private Spinner<Integer> maxScoresSpinner;
  @FXML
  private CheckBox renderRawHighscore;
  @FXML
  private CheckBox renderFriendsHighscore;
  @FXML
  private CheckBox renderPositionsCheckbox;
  @FXML
  private CheckBox renderScoreDatesCheckbox;
  
  @FXML
  private void onFontScoreSelect() {
    CardTemplateBinder templateBeanBinder = templateEditorController.getBeanBinder();
    templateBeanBinder.openFontSelector("score", scoreFontLabel);
  }

  @FXML
  private void onFontScoreApplyAll() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Apply To All", "Apply selected font settings to all templates?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      CardTemplate selection = templateEditorController.getSelectedCardTemplate();
      templateEditorController.applyFontOnAllTemplates(item -> {
        item.setScoreFontName(selection.getScoreFontName());
        item.setScoreFontSize(selection.getScoreFontSize());
        item.setScoreFontStyle(selection.getScoreFontStyle());
      });
    }
  }

  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    setIconVisibility(cardTemplate.isRenderScores());
    setIconLock(cardTemplate.isLockScores(), cardTemplate.isTemplate());

    CardTemplateBinder.setFontLabel(scoreFontLabel, cardTemplate, "score");
    CardTemplateBinder.setColorPickerValue(fontColorSelector, cardTemplate, "fontColor");
    CardTemplateBinder.setColorPickerValue(friendsFontColorSelector, cardTemplate, "friendsFontColor");

    positionController.setTemplate("scores", cardTemplate, res);

    maxScoresSpinner.getValueFactory().setValue(cardTemplate.getMaxScores());
    rowSeparatorSpinner.getValueFactory().setValue(cardTemplate.getRowMargin());
    renderFriendsHighscore.setSelected(cardTemplate.isRenderFriends());
    renderRawHighscore.setSelected(cardTemplate.isRawScore());
    renderPositionsCheckbox.setSelected(cardTemplate.isRenderPositions());
    renderScoreDatesCheckbox.setSelected(cardTemplate.isRenderScoreDates());
  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderScores");
    bindLockIcon(templateBeanBinder, "lockScores");

    friendsFontColorSelector.managedProperty().bindBidirectional(friendsFontColorSelector.visibleProperty());
    renderFriendsHighscore.managedProperty().bindBidirectional(renderFriendsHighscore.visibleProperty());

    friendsFontColorSelector.setVisible(Features.MANIA_ENABLED && Features.MANIA_SOCIAL_ENABLED);
    renderFriendsHighscore.setVisible(Features.MANIA_ENABLED && Features.MANIA_SOCIAL_ENABLED);

    templateBeanBinder.bindColorPicker(fontColorSelector, "fontColor");
    templateBeanBinder.bindColorPicker(friendsFontColorSelector, "friendsFontColor");

    positionController.initBindings("scores", templateBeanBinder);

    templateBeanBinder.bindCheckbox(renderPositionsCheckbox, "renderPositions");
    templateBeanBinder.bindCheckbox(renderScoreDatesCheckbox, "renderScoreDates");

    templateBeanBinder.bindSpinner(maxScoresSpinner, "maxScores", 0, 100);
    templateBeanBinder.bindSpinner(rowSeparatorSpinner, "rowMargin", 0, 300);
    templateBeanBinder.bindCheckbox(renderRawHighscore, "rawScore");

    renderRawHighscore.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      maxScoresSpinner.setDisable(t1);
      renderPositionsCheckbox.setDisable(t1);
      renderScoreDatesCheckbox.setDisable(t1);
      renderFriendsHighscore.setDisable(t1);
    });
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
