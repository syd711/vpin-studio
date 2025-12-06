package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.system.FileInfo;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TableScanProgressModel;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class TableDataTabScoreDataController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final String UNPLAYED_STATUS_ICON = "bi-check2-circle";
  private final static int DEBOUNCE_MS = 300;

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private VBox root;


  @FXML
  private HBox hsFileStatusBox;

  @FXML
  private HBox romStatusBox;

  @FXML
  private TextField altRomName;

  @FXML
  private ComboBox<String> romName;

  @FXML
  private ComboBox<String> highscoreFileName;

  @FXML
  private TextField scannedHighscoreFileName;

  @FXML
  private TextField scannedRomName;

  @FXML
  private TextField scannedAltRomName;

  @FXML
  private Button openHsFileBtn;

  @FXML
  private Button applyAltRomBtn;

  @FXML
  private Button applyRomBtn;

  @FXML
  private Button applyHsBtn;

  @FXML
  private Button resetBtn;

  @FXML
  private Label hsMappingLabel;

  private TableDataController tableDataController;

  private GameRepresentation game;

  @FXML
  private void onScoreReset() {
    TableDialogs.openHighscoresResetDialog(Arrays.asList(game));
  }

  @FXML
  private void onTableScan() {
    ProgressDialog.createProgressDialog(new TableScanProgressModel("Scanning \"" + game.getGameDisplayName() + "\"", Arrays.asList(game)));
    this.game = client.getGame(this.game.getId());
    refreshScannedValues();
  }


  @FXML
  private void onEMHighscore() {
    FileInfo hsFileinfo = client.getGameService().getHighscoreFileInfo(this.game.getId());
    File userFolder = hsFileinfo.getFallback();
    if (!userFolder.exists()) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open EM highscore file, \"User\" folder not found.");
      return;
    }
    try {
      Desktop.getDesktop().open(userFolder);
    }
    catch (IOException e) {
      LOG.error("Failed to open EM highscore file for table " + game.getGameFileName(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open EM highscore file: " + e.getMessage());
    }
  }

  @FXML
  private void onRomApply() {
    romName.setValue(scannedRomName.getText());
  }

  @FXML
  private void onAltRomApply() {
    altRomName.setText(scannedAltRomName.getText());
  }

  @FXML
  private void onHsApply() {
    highscoreFileName.setValue(scannedHighscoreFileName.getText());
  }

  public void setGame(GameRepresentation game, TableDetails tableDetails, ServerSettings serverSettings) {
    this.game = game;

    JFXFuture.supplyAsync(() -> {
      return client.getGameService().getHighscoreFiles(game.getId());
    }).thenAcceptLater((highscoreFiles) -> {
      List<String> availableRoms = new ArrayList<>();
      if (highscoreFiles.getNvRams() != null) {
        availableRoms.addAll(highscoreFiles.getNvRams());
      }
      if (highscoreFiles.getVpRegEntries() != null) {
        availableRoms.addAll(highscoreFiles.getVpRegEntries());
      }
      Collections.sort(availableRoms);
      availableRoms.add(0, null);
      romName.setItems(FXCollections.observableList(availableRoms));

      List<String> availableHsFiles = new ArrayList<>();
      if (highscoreFiles.getTextFiles() != null) {
        availableHsFiles.addAll(highscoreFiles.getTextFiles());
      }
      Collections.sort(availableHsFiles);
      availableHsFiles.add(0, null);
      highscoreFileName.setItems(FXCollections.observableList(availableHsFiles));

      refreshScannedValues();


      applyRomBtn.setDisable(true);
      String tableRomName = tableDetails != null ? tableDetails.getRomName() : null;
      romName.setValue(tableRomName);

      if (StringUtils.isEmpty(tableRomName) && !StringUtils.isEmpty(game.getScannedRom())) {
        if (!StringUtils.isEmpty(game.getRomAlias())) {
          romName.setPromptText(game.getRom() + " (aliased ROM)");
        }
        else {
          romName.setPromptText(game.getScannedRom() + " (scanned value)");
        }

        applyRomBtn.setDisable(false);
      }

      applyAltRomBtn.setDisable(true);
      String tableRomAlt = tableDetails != null ? tableDetails.getRomAlt() : null;
      altRomName.setText(tableRomAlt);

      if (StringUtils.isEmpty(tableRomAlt) && !StringUtils.isEmpty(game.getScannedAltRom())) {
        altRomName.setPromptText(game.getScannedAltRom() + " (scanned value)");
        applyAltRomBtn.setDisable(false);
      }

      resetBtn.setVisible(client.getEmulatorService().isVpxGame(game));

      scannedRomName.setText(game.getScannedRom());
      applyRomBtn.setDisable(StringUtils.isEmpty(scannedRomName.getText()));

      scannedAltRomName.setText(game.getScannedAltRom());
      applyAltRomBtn.setDisable(StringUtils.isEmpty(scannedAltRomName.getText()));

      scannedHighscoreFileName.setText(game.getScannedHsFileName());
      applyHsBtn.setDisable(StringUtils.isEmpty(scannedHighscoreFileName.getText()));
      hsMappingLabel.setText("The value is mapped to Popper field \"" + serverSettings.getMappingHsFileName() + "\"");

      highscoreFileName.setValue(game.getHsFileName());

      tableDataController.setHsFilenameValue(highscoreFileName.getValue());
      if (StringUtils.isEmpty(highscoreFileName.getValue()) && !StringUtils.isEmpty(game.getScannedHsFileName())) {
        highscoreFileName.setPromptText(game.getScannedHsFileName() + " (scanned value)");
      }
    });
  }

  public boolean save() {
    return true;
  }

  public void refreshScannedValues() {
    if (!client.getEmulatorService().isVpxGame(game)) {
      return;
    }

    scannedRomName.setText(game.getScannedRom());
    applyRomBtn.setDisable(StringUtils.isEmpty(game.getScannedRom()));

    scannedAltRomName.setText(game.getScannedAltRom());
    applyAltRomBtn.setDisable(StringUtils.isEmpty(game.getScannedAltRom()));

    scannedHighscoreFileName.setText(game.getScannedHsFileName());
    scannedHighscoreFileName.setPromptText("");

    resetBtn.setDisable(game.getHighscoreType() == null);

    if (!StringUtils.isEmpty(game.getScannedHsFileName()) && !StringUtils.isEmpty(highscoreFileName.getValue())) {
      highscoreFileName.setPromptText(game.getScannedHsFileName() + " (scanned value)");
    }
    else if (StringUtils.isEmpty(game.getScannedHsFileName()) && !StringUtils.isEmpty(game.getScannedAltRom())) {
      //apply scannedAltRom (aka tablename) fallback for scanned highscore field
      scannedHighscoreFileName.setPromptText(game.getScannedAltRom() + ".txt (scanned value)");
    }
    applyHsBtn.setDisable(StringUtils.isEmpty(game.getScannedHsFileName()));
  }


  private void onHighscoreFilenameUpdate(String newValue) {
    tableDataController.setHsFilenameValue(newValue);
    refreshStatusIcons();
  }

  private void onAltRomNameUpdate(String newValue) {
    tableDataController.setTableDetailProperty("romAlt", newValue);
    refreshStatusIcons();
  }

  private void onAltRomNameFocusChange(Boolean newValue) {
    if (!newValue) {
      altRomName.setPromptText("");
      if (StringUtils.isEmpty(altRomName.getText()) && !StringUtils.isEmpty(game.getScannedAltRom())) {
        altRomName.setPromptText(game.getScannedAltRom() + " (scanned value)");
      }
    }
  }

  private void onRomNameUpdate(String newValue) {
    tableDataController.setTableDetailProperty("romName", newValue);
    refreshStatusIcons();
  }


  private void onRomNameFocusChange(Boolean newValue) {
    if (!newValue) {
      romName.setPromptText("");
      if (StringUtils.isEmpty(romName.getValue()) && !StringUtils.isEmpty(game.getScannedRom())) {
        if (!StringUtils.isEmpty(game.getRomAlias())) {
          romName.setPromptText(game.getRom() + " (aliased ROM)");
        }
        else {
          romName.setPromptText(game.getScannedRom() + " (scanned value)");
        }
      }
    }
  }

  private void refreshStatusIcons() {
    // get the score validations with the modified TableDetails (real time check)
    TableDetails tableDetails = tableDataController.getTableDetails();
    JFXFuture.supplyAsync(() -> {
      return client.getGameService().getGameScoreValidation(game.getId(), tableDetails);
    }).thenAcceptLater((gameScoreValidation) -> {
      romStatusBox.getChildren().removeAll(romStatusBox.getChildren());
      hsFileStatusBox.getChildren().removeAll(hsFileStatusBox.getChildren());

      String statusRom = gameScoreValidation.getRomStatus();
      if (statusRom != null) {
        FontIcon icon = WidgetFactory.createIcon(gameScoreValidation.getRomIcon());
        icon.setIconColor(javafx.scene.paint.Paint.valueOf(gameScoreValidation.getRomIconColor()));
        Label l = new Label();
        l.setGraphic(icon);
        l.setTooltip(new Tooltip(statusRom));
        romStatusBox.getChildren().add(l);
      }

      String statusHsFile = gameScoreValidation.getHighscoreFilenameStatus();
      if (statusHsFile != null) {
        FontIcon icon = WidgetFactory.createIcon(gameScoreValidation.getHighscoreFilenameIcon());
        icon.setIconColor(Paint.valueOf(gameScoreValidation.getHighscoreFilenameIconColor()));
        Label l = new Label();
        l.setGraphic(icon);
        l.setTooltip(new Tooltip(statusHsFile));
        hsFileStatusBox.getChildren().add(l);
      }
    });
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    openHsFileBtn.setVisible(client.getSystemService().isLocal());
  }

  public void initBindings(TableDataController tableDataController) {
    this.tableDataController = tableDataController;

    romName.valueProperty().addListener((observable, oldValue, newValue) -> {
      onRomNameUpdate(newValue);
    });
    romName.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        onRomNameFocusChange(newValue);
      }
    });

    romName.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce("romName", () -> {
        onRomNameUpdate(newValue);
      }, DEBOUNCE_MS, true);
    });

    altRomName.textProperty().addListener((observable, oldValue, newValue) -> {
      onAltRomNameUpdate(newValue);
    });
    altRomName.focusedProperty().addListener((observable, oldValue, newValue) -> onAltRomNameFocusChange(newValue));

    highscoreFileName.valueProperty().addListener((observable, oldValue, newValue) -> {
      onHighscoreFilenameUpdate(newValue);
    });
    highscoreFileName.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce("highscoreFileName", () -> {
        onHighscoreFilenameUpdate(newValue);
      }, DEBOUNCE_MS, true);
    });
  }
}
