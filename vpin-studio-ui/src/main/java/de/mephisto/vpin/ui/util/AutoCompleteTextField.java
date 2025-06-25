package de.mephisto.vpin.ui.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.commons.utils.JFXFuture;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This class is a TextField which implements an "autocomplete" functionality, based on a supplied list of entries.
 *
 * @author Caleb Brinkman
 */
public class AutoCompleteTextField {
  private final static Logger LOG = LoggerFactory.getLogger(AutoCompleteTextField.class);
  private final ContextMenu entriesPopup;
  private final TextField textField;
  private final AutoCompleteTextFieldChangeListener listener;
  private final AutoCompleteMatcher matcher;

  private boolean changedEnabled = true;

  private String defaultValue;

  public AutoCompleteTextField(Stage stage, TextField textField, AutoCompleteTextFieldChangeListener listener, TreeSet<String> entries) {
    this(stage, textField, listener, 
      input -> entries.stream()
                      .filter(e -> StringUtils.containsIgnoreCase(e, input))
                      .map(e -> new AutoMatchModel(e, e))
                      .collect(Collectors.toList()));
  }

  public AutoCompleteTextField(Stage stage, TextField textField, AutoCompleteTextFieldChangeListener listener, AutoCompleteMatcher matcher) {
    this.textField = textField;
    this.listener = listener;
    this.matcher = matcher;

    entriesPopup = new ContextMenu();
    entriesPopup.getStyleClass().add("context-menu");

    entriesPopup.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      public void handle(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER && e.getTarget().getClass().getSimpleName().startsWith("MenuItemContainer")) {
          try {
            String value = (String) PropertyUtils.getProperty(e.getTarget(), "id");
            defaultValue = value;
            entriesPopup.hide();
            entriesPopup.getItems().clear();
            textField.setText(String.valueOf(value));
            listener.onChange(value);
            Platform.runLater(() -> {
              textField.getParent().requestFocus();
            });
          }
          catch (Exception ex) {
            LOG.error("Auto-complete error: " + ex.getMessage(), ex);
          }
          e.consume();
        }
      }
    });

    textField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
        if (!changedEnabled) {
          entriesPopup.hide();
          return;
        }

        if (textField.getText().length() == 0) {
          entriesPopup.hide();
        }
        else {
          List<AutoMatchModel> searchResult = matcher.match(textField.getText());
          if (!searchResult.isEmpty()) {
            populatePopup(searchResult);
            showPopup();
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
        if (!newValue) {
          setText(String.valueOf(defaultValue));
        }
      }
    });
  }

  public void setChangeEnabled(boolean b) {
    this.changedEnabled = b;
  }

  /**
   * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
   *
   * @param searchResult The set of matching strings.
   */
  private void populatePopup(List<AutoMatchModel> searchResult) {
    List<MenuItem> menuItems = new LinkedList<>();
    // If you'd like more entries, modify this line.
    int maxEntries = 10;
    int count = Math.min(searchResult.size(), maxEntries);
    for (int i = 0; i < count; i++) {
      final String result = String.valueOf(searchResult.get(i));
      Label entryLabel = new Label(result);

      MenuItem item = new MenuItem("", entryLabel);
      item.setId(searchResult.get(i).getId());
      entryLabel.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
          textField.setText(entryLabel.getText());
          entriesPopup.hide();
          defaultValue = result;
          listener.onChange(item.getId());
        }
      });
//      item.setOnAction(new EventHandler<ActionEvent>() {
//        @Override
//        public void handle(ActionEvent actionEvent) {
//          textField.setText(result);
//          entriesPopup.hide();
//          defaultValue = result;
//          listener.onChange(result);
//        }
//      });
      menuItems.add(item);
    }
    entriesPopup.getItems().clear();
    entriesPopup.getItems().addAll(menuItems);
  }

  /**
   * Display the popup
   */
  private void showPopup() {
    if (!entriesPopup.isShowing()) {
      entriesPopup.show(textField, Side.BOTTOM, 0, 0);
      entriesPopup.requestFocus();
    }
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

  /**
   * Trigger the autoselectionn and if result is one, automatically select it
   */
  public void selectIfMatch() {
    if (textField.getText().length() > 0) {
      JFXFuture.supplyAsync(() -> matcher.match(textField.getText()))
        .thenAcceptLater(searchResult -> {
            if (searchResult.size() == 1) {
              AutoMatchModel match = searchResult.get(0);
              String value = match.getId();
              defaultValue = value;
              setText(value);
              listener.onChange(value);
            }
            else if (searchResult.size() > 1) {
              populatePopup(searchResult);
              showPopup();
            }
          });
    }
  }

  public void setDisable(boolean b) {
    textField.setDisable(b);
  }

  public void focus() {
    textField.requestFocus();
  }
}