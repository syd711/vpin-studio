package de.mephisto.vpin.ui.tournaments.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.tables.vps.VpsTableVersionCell;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoCompleteTextFieldChangeListener;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TournamentTableSelectorDialogController implements DialogController, AutoCompleteTextFieldChangeListener, Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentTableSelectorDialogController.class);

  @FXML
  private TextField nameField;

  @FXML
  private CheckBox statusCheckbox;

  @FXML
  private CheckBox customTimeCheckbox;

  @FXML
  private Label durationLabel;

  @FXML
  private DatePicker startDatePicker;

  @FXML
  private DatePicker endDatePicker;

  @FXML
  private ComboBox<String> startTime;

  @FXML
  private ComboBox<String> endTime;

  @FXML
  private Pane validationContainer;

  @FXML
  private Label validationTitle;

  @FXML
  private Button okButton;

  private AutoCompleteTextField autoCompleteNameField;

  @FXML
  private ComboBox<VpsTableVersion> versionsCombo;

  private Tournament tournament;
  private TournamentTable tournamentTable;

  @Override
  public void onDialogCancel() {
    this.tournamentTable = null;
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    tournamentTable = null;
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
    List<VpsTable> tables = client.getVpsService().getTables();
    Optional<VpsTable> first = tables.stream().filter(t -> t.getDisplayName().equalsIgnoreCase(value)).findFirst();
    okButton.setDisable(!first.isPresent());

    if (first.isPresent()) {
      VpsTable vpsTable = first.get();
      tournamentTable.setVpsTableId(vpsTable.getId());
      tournamentTable.setDisplayName(vpsTable.getDisplayName());

      List<VpsTableVersion> tableFiles = new ArrayList<>(vpsTable.getTableFiles());
      tableFiles.add(0, null);

      versionsCombo.setItems(FXCollections.emptyObservableList());
      versionsCombo.setItems(FXCollections.observableList(tableFiles));
      if (tableFiles.size() > 1) {
        versionsCombo.getSelectionModel().select(1);
        tournamentTable.setVpsVersionId(versionsCombo.getValue().getId());
      }

      validate();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    versionsCombo.setCellFactory(c -> new VpsTableVersionCell());
    versionsCombo.setButtonCell(new VpsTableVersionCell());
    versionsCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        tournamentTable.setVpsVersionId(newValue.getId());
      }
      else {
        tournamentTable.setVpsVersionId(null);
      }
      validate();
    });

    statusCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        tournamentTable.setEnabled(newValue);
        validate();
      }
    });

    startDatePicker.setValue(LocalDate.now());
    endDatePicker.setValue(LocalDate.now().plus(7, ChronoUnit.DAYS));

    customTimeCheckbox.setSelected(false);
    startDatePicker.setDisable(true);
    startTime.setDisable(true);
    endDatePicker.setDisable(true);
    endTime.setDisable(true);

    Platform.runLater(() -> nameField.requestFocus());
  }

  public void setTournamentTable(Stage stage, Tournament tournament, TournamentTable tournamentTable) {
    List<VpsTable> tables = client.getVpsService().getTables();
    List<String> collect = new ArrayList<>(tables.stream().map(t -> t.getDisplayName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(this.nameField, this, collect);

    this.tournament = tournament;
    this.tournamentTable = tournamentTable;
    this.customTimeCheckbox.setSelected(tournamentTable.getStartDate() != null);
    this.statusCheckbox.setSelected(tournamentTable.isEnabled());
    customTimeCheckbox.setSelected(customTimeCheckbox.isSelected());

    startDatePicker.setDisable(!customTimeCheckbox.isSelected());
    startTime.setDisable(!customTimeCheckbox.isSelected());
    endDatePicker.setDisable(!customTimeCheckbox.isSelected());
    endTime.setDisable(!customTimeCheckbox.isSelected());


    startTime.setItems(FXCollections.observableList(DateUtil.TIMES));
    startTime.setValue("00:00");
    if (tournamentTable.getStartDate() != null) {
      this.startDatePicker.setValue(tournamentTable.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.startDatePicker.setDisable(!customTimeCheckbox.isSelected());
      this.startTime.setValue(DateUtil.formatTimeString(tournamentTable.getStartDate()));
      this.startTime.setDisable(!customTimeCheckbox.isSelected());
    }

    endTime.setItems(FXCollections.observableList(DateUtil.TIMES));
    endTime.setValue("00:00");
    if (tournamentTable.getEndDate() != null) {
      this.endDatePicker.setValue(tournamentTable.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.endDatePicker.setDisable(!customTimeCheckbox.isSelected());
      this.endTime.setValue(DateUtil.formatTimeString(tournamentTable.getEndDate()));
      this.endTime.setDisable(!customTimeCheckbox.isSelected());
    }

    this.statusCheckbox.setSelected(tournamentTable.isEnabled());


    VpsTable vpsTable = client.getVpsService().getTableById(tournamentTable.getVpsTableId());
    if (vpsTable != null) {
      this.nameField.setText(vpsTable.getName());
      this.autoCompleteNameField.setText(vpsTable.getName());
      List<VpsTableVersion> tableFiles = new ArrayList<>(vpsTable.getTableFiles());
      tableFiles.add(0, null);
      versionsCombo.setItems(FXCollections.observableList(tableFiles));

      VpsTableVersion vpsTableVersion = vpsTable.getTableVersionById(tournamentTable.getVpsVersionId());
      if (vpsTableVersion != null) {
        versionsCombo.setValue(vpsTableVersion);
      }
    }

    startDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      Date date = DateUtil.formatDate(startDatePicker.getValue(), startTime.getValue());
      tournamentTable.setStartDate(date);
      validate();
    });
    startTime.valueProperty().addListener((observable, oldValue, newValue) -> {
      Date date = DateUtil.formatDate(startDatePicker.getValue(), startTime.getValue());
      tournamentTable.setStartDate(date);
      validate();
    });

    endDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      Date date = DateUtil.formatDate(endDatePicker.getValue(), endTime.getValue());
      tournamentTable.setEndDate(date);
      validate();
    });
    endTime.valueProperty().addListener((observable, oldValue, newValue) -> {
      Date date = DateUtil.formatDate(endDatePicker.getValue(), endTime.getValue());
      tournamentTable.setEndDate(date);
      validate();
    });


    customTimeCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        startDatePicker.setDisable(!newValue);
        startTime.setDisable(!newValue);
        endDatePicker.setDisable(!newValue);
        endTime.setDisable(!newValue);

        if (newValue && tournamentTable.getStartDate() == null) {
          Date startDate = DateUtil.formatDate(startDatePicker.getValue(), startTime.getValue());
          tournamentTable.setStartDate(startDate);

          Date endDate = DateUtil.formatDate(endDatePicker.getValue(), endTime.getValue());
          tournamentTable.setEndDate(endDate);
        }

        validate();
      }
    });

    validate();
  }

  private void validate() {
    validationContainer.setVisible(true);
    this.okButton.setDisable(true);

    if (customTimeCheckbox.isSelected()) {
      if (tournamentTable.getStartDate().getTime() >= tournamentTable.getEndDate().getTime()) {
        validationTitle.setText("Invalid start/end date set: The end date must be after the start date.");
        return;
      }

      if (tournamentTable.getStartDate().getTime() < tournament.getStartDate().getTime()) {
        validationTitle.setText("Invalid start date set: The start is not within the tournaments time range.");
        return;
      }

      if (tournamentTable.getEndDate().getTime() > tournament.getEndDate().getTime()) {
        validationTitle.setText("Invalid end date set: The start is not within the tournaments time range.");
        return;
      }
    }


    Date startDate = tournamentTable.getStartDate();
    Date endDate = tournamentTable.getEndDate();
    this.durationLabel.setText(DateUtil.formatDuration(startDate, endDate));

    if (tournamentTable.getVpsTableId() == null) {
      validationTitle.setText("No table selected.");
      return;
    }

    if (tournamentTable.getVpsVersionId() == null) {
      validationTitle.setText("No table version selected.");
      return;
    }

    validationContainer.setVisible(false);
    this.okButton.setDisable(false);
  }

  public TournamentTable getTournamentTable() {
    return tournamentTable;
  }
}
