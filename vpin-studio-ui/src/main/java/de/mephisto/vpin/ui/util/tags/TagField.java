package de.mephisto.vpin.ui.util.tags;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoCompleteTextFieldChangeListener;
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
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

public class TagField extends VBox implements AutoCompleteTextFieldChangeListener {

  private final TextField inputField;

  private final ObservableList<String> tags = FXCollections.observableList(new ArrayList<>());
  private final FlowPane tagContainer;
  private final AutoCompleteTextField autoCompleteTextField;
  private double tagWidth;
  private boolean customTags = true;
  private double height;

  public TagField(List<String> suggestions) {
    super(6);
    // TextField for input
    inputField = new TextField();
    autoCompleteTextField = new AutoCompleteTextField(inputField, this, suggestions);
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

    tagContainer = new FlowPane();
    tagContainer.setHgap(8);
    tagContainer.setVgap(8);
    tagContainer.setOpaqueInsets(new Insets(12, 0, 0, 0));
    this.getChildren().addAll(inputField, tagContainer);
    this.setPadding(new Insets(0));

    tags.addListener(new ListChangeListener<String>() {
      @Override
      public void onChanged(Change<? extends String> c) {
        tagContainer.getChildren().removeAll(tagContainer.getChildren());
        for (String tag : tags) {
          if (!StringUtils.isEmpty(tag)) {
            HBox tagBox = createTag(tag);
            tagContainer.getChildren().add(tagBox);
          }
        }

        clearInput();
      }
    });
  }

  public void setPreferredHeight(double height) {
    this.height = height;
    tagContainer.setMaxHeight(this.height);
  }

  public void setPreferredWidth(double width) {
    setPrefWidth(width);
    inputField.setMaxWidth(width);
    tagContainer.setMaxWidth(width);
    setPreferredTagWidth(width);
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
    tags.clear();
    tags.addAll(values);
    clearInput();
  }

  public void toggleTag(String tagsValue) {
    if (!tags.contains(tagsValue)) {
      tags.add(tagsValue);
    }
    else {
      tags.remove(tagsValue);
    }
  }

  private HBox createTag(String tag) {
    HBox tagBox = new HBox();
    if (tagWidth > 0) {
      tagBox.setMaxWidth(tagWidth);
    }

    tagBox.setStyle("-fx-background-color: " + TaggingUtil.getColor(getTags(), tag) + "; -fx-background-radius: 12; -fx-padding: 3 10;");
    tagBox.setSpacing(3);

    Label tagText = new Label(tag);
    tagText.getStyleClass().add("default-text");
    Button removeButton = new Button();
    removeButton.getStyleClass().add("ghost-button-tiny");
    removeButton.setStyle("-fx-font-color: #FFFFFF");

    FontIcon icon = WidgetFactory.createIcon("mdi2w-window-close");
    removeButton.setGraphic(icon);
    removeButton.setOnAction(e -> {
      tags.remove(tag);
      clearInput();
    });

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
          autoCompleteTextField.reset();
          inputField.requestFocus();
        });
      });
    });
  }

  public void setAllowCustomTags(boolean customTags) {
    this.customTags = customTags;
  }

  public void focus() {
    this.inputField.requestFocus();
  }
}
