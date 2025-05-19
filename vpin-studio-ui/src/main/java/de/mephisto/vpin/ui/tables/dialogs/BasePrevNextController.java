package de.mephisto.vpin.ui.tables.dialogs;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.DialogHeaderController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.Studio;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;

/**
 * A base dialog class that support Next / prev and autosave buttons
 */
public abstract class BasePrevNextController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(BaseUploadController.class);

  @FXML
  protected DialogHeaderController headerController;  //fxml magic! Not unused -> id + "Controller"

  @FXML
  protected Button prevButton;
  @FXML
  protected Button nextButton;
  @FXML
  protected CheckBox autosaveCheckbox;

  //------------------------------------------

  @FXML
  protected void onPrevious(ActionEvent e) {
    onAutosave(() -> openPrev());
  }

  @FXML
  protected void onNext(ActionEvent e) {
    onAutosave(() -> openNext());
  }

 protected void onAutosave(@Nullable Runnable onSuccess) {
    if (autosaveCheckbox.isSelected()) {
      autosave(onSuccess);
    }
    else if (headerController.isDirty()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "you have unsaved changes, do you want to save it ?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        autosave(onSuccess);
        return;
      }
      else {
        // click cancel, stay on the table
        return;
      }
    }
    else {
      onSuccess.run();
    }
  }

  /**
   * Method called when click on Previous button, Open and display previous Object
   */
  protected abstract void openPrev();

  /**
   * Method called when click on Next button, Open and display next Object
   */
  protected abstract void openNext();

  /**
   * Method called on autosave, Save current Object, must call onSuccess if successfully
   */
  protected abstract void autosave(@Nullable Runnable onSuccess);

}