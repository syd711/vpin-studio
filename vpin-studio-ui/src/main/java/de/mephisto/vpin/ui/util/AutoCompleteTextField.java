package de.mephisto.vpin.ui.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is a TextField which implements an "autocomplete" functionality, based on a supplied list of entries.
 *
 * @author Caleb Brinkman
 */
public class AutoCompleteTextField {
  private final AutoCompleteTextFieldChangeListener listener;
  private final SortedSet<String> entries;
  private final ContextMenu entriesPopup;
  private final TextField textField;

  private boolean changedEnabled;

  private String defaultValue;

  /**
   * Construct a new AutoCompleteTextField.
   */
  public AutoCompleteTextField(TextField textField, AutoCompleteTextFieldChangeListener listener, TreeSet<String> entries) {
    super();
    this.textField = textField;
    this.listener = listener;
    this.entries = entries;
    entriesPopup = new ContextMenu();
    textField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
        if(!changedEnabled) {
          entriesPopup.hide();
          return;
        }

        if (textField.getText().length() == 0) {
          entriesPopup.hide();
        }
        else {
          List<String> searchResult = entries.stream().filter(e -> e.toLowerCase().startsWith(textField.getText().toLowerCase())).collect(Collectors.toList());
//          searchResult.addAll(entries.subSet(textField.getText(), textField.getText() + Character.MAX_VALUE));
          if (entries.size() > 0) {
            populatePopup(searchResult);
            if (!entriesPopup.isShowing()) {
              entriesPopup.show(textField, Side.BOTTOM, 0, 0);
            }
          }
          else {
            entriesPopup.hide();
          }
        }
      }
    });

    textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
        entriesPopup.hide();
      }
    });

    textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
          setText(defaultValue);
        }
      }
    });

    textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if(!newValue) {
          setText(defaultValue);
        }
      }
    });
  }

  public void setChangeEnabled(boolean b) {
    this.changedEnabled = b;
  }

  /**
   * Get the existing set of autocomplete entries.
   *
   * @return The existing autocomplete entries.
   */
  public SortedSet<String> getEntries() {
    return entries;
  }

  /**
   * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
   *
   * @param searchResult The set of matching strings.
   */
  private void populatePopup(List<String> searchResult) {
    List<CustomMenuItem> menuItems = new LinkedList<>();
    // If you'd like more entries, modify this line.
    int maxEntries = 10;
    int count = Math.min(searchResult.size(), maxEntries);
    for (int i = 0; i < count; i++) {
      final String result = searchResult.get(i);
      Label entryLabel = new Label(result);
      CustomMenuItem item = new CustomMenuItem(entryLabel, true);
      item.setStyle("-fx-padding: 3px 3px 3px 3px;-fx-font-size: 14px;");
      item.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
          textField.setText(result);
          entriesPopup.hide();
          defaultValue = result;
          listener.onChange(result);
        }
      });
      menuItems.add(item);
    }
    entriesPopup.getItems().clear();
    entriesPopup.getItems().addAll(menuItems);

  }

  public void reset() {
    setChangeEnabled(false);
    textField.setText("");
    setChangeEnabled(true);
  }

  public void setText(String name) {
    setChangeEnabled(false);
    textField.setText(name);
    defaultValue = name;
    setChangeEnabled(true);
  }

  public void setDisable(boolean b) {
    textField.setDisable(b);
  }
}