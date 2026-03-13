package de.mephisto.vpin.ui.util.tags;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.ui.events.EventManager;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TagButton extends HBox {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Label tagText;

  public TagButton(int gameId, @NonNull TableDetails tableDetails, @NonNull List<String> tags, @NonNull String tag) {
    String color = TaggingUtil.getColor(tags, tag);
    setStyle("-fx-background-color: " + color + "; -fx-background-radius: 12; -fx-padding: 2 8 2 8;-fx-cursor: hand;");
    setSpacing(3);

    tagText = new Label(tag);
    tagText.getStyleClass().add("default-text");
    getChildren().addAll(tagText);

    Button button = new Button();
    button.getStyleClass().add("ghost-button-tiny");
    button.setGraphic(WidgetFactory.createIcon("mdi2w-window-close"));
    button.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        try {
          List<String> tableTags = TaggingUtil.getTags(tableDetails.getTags());
          tableTags.remove(tag);
          tableDetails.setTags(String.join(",", tableTags));
          client.getFrontendService().saveTableDetails(tableDetails, gameId);
          EventManager.getInstance().notifyTableChange(gameId, null);
        }
        catch (Exception e) {
          LOG.error("Failed to remove tag: {}", e.getMessage(), e);
        }
      }
    });
    getChildren().add(button);
  }

  public void setButtonListener(EventHandler<MouseEvent> handler) {
    tagText.setOnMouseClicked(handler);
  }
}
