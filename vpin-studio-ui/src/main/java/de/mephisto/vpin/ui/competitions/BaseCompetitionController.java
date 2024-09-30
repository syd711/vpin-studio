package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.ui.StudioFXController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

abstract public class BaseCompetitionController implements StudioFXController {

  @FXML
  private Button clearBtn;

  @FXML
  protected TextField textfieldSearch;

  @FXML
  private void onClear() {
    textfieldSearch.setText("");
  }

  @Override
  public void onKeyEvent(KeyEvent event) {
    if (event.getCode() == KeyCode.F && event.isControlDown()) {
      textfieldSearch.requestFocus();
      textfieldSearch.selectAll();
      event.consume();
    }
    else if (event.getCode() == KeyCode.ESCAPE) {
      if (textfieldSearch.isFocused()) {
        textfieldSearch.setText("");
      }
      event.consume();
    }
  }

  public void initialize() {
    clearBtn.setVisible(false);
    textfieldSearch.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        clearBtn.setVisible(newValue == null || !newValue.isEmpty());
      }
    });
  }
}
