package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.ui.StudioFXController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

abstract public class BasePlayersController implements StudioFXController {

  @FXML
  private Button clearBtn;

  @FXML
  protected TextField searchTextField;

  @FXML
  private void onClear() {
    searchTextField.setText("");
  }

  public void initialize() {
    clearBtn.setVisible(false);
    searchTextField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        clearBtn.setVisible(newValue == null || !newValue.isEmpty());
      }
    });
  }

  @Override
  public void onKeyEvent(KeyEvent event) {
    if (event.getCode() == KeyCode.F && event.isControlDown()) {
      searchTextField.requestFocus();
      searchTextField.selectAll();
      event.consume();
    }
    else if (event.getCode() == KeyCode.ESCAPE) {
      if (searchTextField.isFocused()) {
        searchTextField.setText("");
      }
      event.consume();
    }
  }
}
