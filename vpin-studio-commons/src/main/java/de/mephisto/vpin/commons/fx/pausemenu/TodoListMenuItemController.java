package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.tagging.TaggingSettings;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.fx.ServerFX.client;

public class TodoListMenuItemController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private VBox column1;

  @FXML
  private VBox column2;

  private final List<CheckBox> checkBoxes = new ArrayList<>();
  private int selectedIndex = 0;

  public void setData(GameRepresentation game, PauseMenuItem pauseMenuItem) {
    TableDetails tableDetails = client.getFrontendService().getTableDetails(game.getId());
    String tags = tableDetails.getTags();
    List<String> tagList = TaggingUtil.getTags(tags);

    for (CheckBox cb : checkBoxes) {
      String tag = cb.getText();
      cb.setSelected(tagList.contains(tag));
      cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal) {
          if (!tagList.contains(tag)) {
            tagList.add(tag);
          }
        } else {
          tagList.remove(tag);
        }
        tableDetails.setTags(TaggingUtil.join(tagList));
        JFXFuture.supplyAsync(() -> {
          try {
            return client.getFrontendService().saveTableDetails(tableDetails, game.getId());
          } catch (Exception e) {
            LOG.error("Failed to save table details for game {}", game.getId(), e);
            return null;
          }
        });
      });
    }

    if (!checkBoxes.isEmpty()) {
      applySelection(0);
    }
  }

  private void applySelection(int newIndex) {
    if (selectedIndex >= 0 && selectedIndex < checkBoxes.size()) {
      checkBoxes.get(selectedIndex).setStyle("-fx-text-fill: white; -fx-font-size: 36px;");
    }
    selectedIndex = newIndex;
    if (selectedIndex >= 0 && selectedIndex < checkBoxes.size()) {
      checkBoxes.get(selectedIndex).setStyle("-fx-text-fill: #6666FF; -fx-font-size: 36px;");
    }
  }

  public boolean right() {
    if (checkBoxes.isEmpty()) {
      return false;
    }
    if (selectedIndex < checkBoxes.size() - 1) {
      applySelection(selectedIndex + 1);
      return true;
    }
    return false;
  }

  public boolean left() {
    if (checkBoxes.isEmpty()) {
      return false;
    }
    if (selectedIndex > 0) {
      applySelection(selectedIndex - 1);
      return true;
    }
    return false;
  }

  public void enter() {
    if (!checkBoxes.isEmpty() && selectedIndex >= 0 && selectedIndex < checkBoxes.size()) {
      CheckBox cb = checkBoxes.get(selectedIndex);
      cb.setSelected(!cb.isSelected());
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TaggingSettings taggingSettings = ServerFX.client.getJsonPreference(PreferenceNames.TAGGING_SETTINGS, TaggingSettings.class);
    List<String> pauseMenuTags = taggingSettings.getPauseMenuTags();

    int count = Math.min(pauseMenuTags.size(), TaggingSettings.MAX_TODO_TAGS);
    for (int i = 0; i < count; i++) {
      CheckBox cb = new CheckBox(pauseMenuTags.get(i));
      cb.getStyleClass().add("base-component");
      cb.setStyle("-fx-text-fill: white; -fx-font-size: 36px;");
      checkBoxes.add(cb);
      if (i < TaggingSettings.MAX_TODO_TAGS / 2) {
        column1.getChildren().add(cb);
      } else {
        column2.getChildren().add(cb);
      }
    }
  }
}
