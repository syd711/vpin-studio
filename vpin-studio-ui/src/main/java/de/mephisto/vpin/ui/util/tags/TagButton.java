package de.mephisto.vpin.ui.util.tags;

import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.List;

public class TagButton extends HBox {

  public TagButton(@NonNull List<String> tags, @NonNull String tag) {
    String color = TaggingUtil.getColor(tags, tag);
    setStyle("-fx-background-color: " + color + "; -fx-background-radius: 12; -fx-padding: 2 8 2 8;-fx-cursor: hand;");
    setSpacing(3);

    Label tagText = new Label(tag);
    tagText.getStyleClass().add("default-text");
    getChildren().addAll(tagText);
  }
}
