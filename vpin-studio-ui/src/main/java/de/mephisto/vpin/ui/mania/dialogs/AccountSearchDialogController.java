package de.mephisto.vpin.ui.mania.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.ui.util.AutoCompleteMatcher;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoCompleteTextFieldChangeListener;
import de.mephisto.vpin.ui.util.AutoMatchModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class AccountSearchDialogController implements DialogController, AutoCompleteTextFieldChangeListener, Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(AccountSearchDialogController.class);

  @FXML
  private TextField nameField;

  @FXML
  private Button okButton;

  private AutoCompleteTextField autoCompleteNameField;

  private Account selection;
  private Stage stage;
  private List<Account> accounts;

  @Override
  public void onDialogCancel() {

  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDialogSubmit(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void onChange(String value) {
    try {
      List<Account> accs = maniaClient.getAccountClient().searchAccounts(value);
      if (!accs.isEmpty()) {
        this.selection = accs.get(0);
      }
    }
    catch (Exception e) {
      LOG.error("Account search failed: " + e.getMessage(), e);
    }
    stage.close();
  }

  public void setStage(Stage stage) {
    this.stage = stage;

    autoCompleteNameField = new AutoCompleteTextField(stage, this.nameField, this, null, new AutoCompleteMatcher() {
      @Override
      public List<AutoMatchModel> match(String input) {
        try {
          return maniaClient.getAccountClient().searchAccounts(input).stream().map(a -> new AutoMatchModel(a.getDisplayName(), a.getUuid())).collect(Collectors.toList());
        }
        catch (Exception e) {
          LOG.error("Account search failed: " + e.getMessage(), e);
        }
        return Collections.emptyList();
      }
    });

    autoCompleteNameField.focus();
  }

  public Account getSelection() {
    return selection;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }
}
