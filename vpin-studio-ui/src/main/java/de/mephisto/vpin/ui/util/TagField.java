package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

public class TagField extends FlowPane implements AutoCompleteTextFieldChangeListener {

  private final TextField inputField;

  private final ObservableList<String> tags = FXCollections.observableList(new ArrayList<>());
  private double tagWidth;
  private boolean customTags = true;

  public TagField(Pane tagContainer, List<String> suggestions) {
    super(0, 0);
    // TextField for input
    inputField = new TextField();
    AutoCompleteTextField autoCompleteTextField = new AutoCompleteTextField(inputField, this, suggestions);
    inputField.setPrefWidth(400);
    inputField.setStyle("-fx-font-size: 14px");
    inputField.setPromptText("Hit Enter to apply new tags...");

    // When Enter is pressed, create a new tag
    inputField.setOnAction(e -> {
      String text = inputField.getText().trim();
      if (!text.isEmpty()) {
        if (!suggestions.contains(text.trim()) && !customTags) {
          return;
        }

        if (tags.contains(text.trim())) {
          return;
        }
        tags.add(text.trim());
        clearInput();
      }
    });

    this.getChildren().addAll(inputField, tagContainer);
    this.setPadding(new Insets(0));

    tags.addListener(new ListChangeListener<String>() {
      @Override
      public void onChanged(Change<? extends String> c) {
        tagContainer.getChildren().removeAll(tagContainer.getChildren());
        for (String tag : tags) {
          if (!StringUtils.isEmpty(tag)) {
            tagContainer.getChildren().add(createTag(tag));
          }
        }
        clearInput();
      }
    });
  }

  public void setPreferredWidth(double width) {
    inputField.setPrefWidth(width);
  }


  public void setPreferredTagWidth(double tagWidth) {
    this.tagWidth = tagWidth;
  }

  public void addListener(ListChangeListener<String> listener) {
    tags.addListener(listener);
  }

  public void removeListener(ListChangeListener<String> listener) {
    tags.removeListener(listener);
  }

  public List<String> getTags() {
    return new ArrayList<>(this.tags);
  }

  public void setTags(List<String> values) {
    tags.addAll(values);
  }

  // Create a tag with text and remove button
  private HBox createTag(String tag) {
    HBox tagBox = new HBox();
    if (tagWidth > 0) {
      tagBox.setMaxWidth(tagWidth);
    }

    int index = tags.indexOf(tag) % TaggingUtil.COLORS.size();
    String color = TaggingUtil.COLORS.get(index);
    tagBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 12; -fx-padding: 3 10;");
    tagBox.setSpacing(3);

    Label tagText = new Label(tag);
    tagText.getStyleClass().add("default-text");
    Button removeButton = new Button();
    removeButton.getStyleClass().add("ghost-button-tiny");
    removeButton.setStyle("-fx-font-color: #FFFFFF");

    FontIcon icon = WidgetFactory.createIcon("mdi2w-window-close");
    removeButton.setGraphic(icon);
    removeButton.setOnAction(e -> tags.remove(tag));

    tagBox.getChildren().addAll(tagText, removeButton);
    return tagBox;
  }

  @Override
  public void onChange(String value) {
    if (!tags.contains(value.trim())) {
      tags.add(value.trim());
      clearInput();
    }
  }

  private void clearInput() {
    Platform.runLater(() -> {
      inputField.requestFocus();
      Platform.runLater(() -> {
        inputField.clear();
        Platform.runLater(() -> {
          inputField.requestFocus();
        });
      });
    });
  }

  public void setAllowCustomTags(boolean customTags) {
    this.customTags = customTags;
  }
}
