package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.TableManifest;
import de.mephisto.vpin.restclient.VpaPackageInfo;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class RepositorySidebarController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(RepositorySidebarController.class);

  @FXML
  private Accordion repositoryAccordion;

  @FXML
  private TitledPane detailsPane;

  @FXML
  private VBox repositoryAccordionVBox;

  @FXML
  private Label sourceLabel;

  @FXML
  private Label filenameLabel;

  @FXML
  private Label fileSizeLabel;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label idLabel;

  @FXML
  private Label repositoryTypeLabel;

  @FXML
  private Label repositoryNameLabel;

  @FXML
  private Button idCopyBtn;

  @FXML
  private FontIcon directb2sIcon;

  @FXML
  private FontIcon pupPackIcon;

  @FXML
  private FontIcon romIcon;

  @FXML
  private FontIcon cfgIcon;

  @FXML
  private FontIcon popperIcon;

  @FXML
  private FontIcon flexIcon;

  @FXML
  private FontIcon ultraIcon;

  @FXML
  private FontIcon musicIcon;

  @FXML
  private FontIcon altSoundIcon;
  @FXML
  private FontIcon altColorIcon;

  @FXML
  private FontIcon povIcon;

  @FXML
  private FontIcon resIcon;

  @FXML
  private FontIcon highscoreIcon;

  @FXML
  private Label highscoreHistoryLabel;

  @FXML
  private TitledPane manifestPane;

  @Override
  public void onViewActivated() {

  }

  @FXML
  private void onIdCopy() {
    String id = this.idLabel.getText();
    if(!StringUtils.isEmpty(id) && id.length() > 1) {
      Clipboard systemClipboard = Clipboard.getSystemClipboard();
      final ClipboardContent content = new ClipboardContent();
      content.putString(id);
      systemClipboard.setContent(content);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    repositoryAccordion.managedProperty().bindBidirectional(repositoryAccordion.visibleProperty());
    repositoryAccordion.setExpandedPane(detailsPane);
    setVpaDescriptor(Optional.empty());
  }

  public void setVisible(boolean b) {
    this.repositoryAccordion.setVisible(b);
  }

  public void setVpaDescriptor(Optional<VpaDescriptorRepresentation> selection) {
    directb2sIcon.setVisible(false);
    pupPackIcon.setVisible(false);
    romIcon.setVisible(false);
    popperIcon.setVisible(false);
    flexIcon.setVisible(false);
    ultraIcon.setVisible(false);
    musicIcon.setVisible(false);
    altSoundIcon.setVisible(false);
    altColorIcon.setVisible(false);
    resIcon.setVisible(false);
    cfgIcon.setVisible(false);
    povIcon.setVisible(false);
    highscoreIcon.setVisible(false);
    idCopyBtn.setVisible(false);

    highscoreHistoryLabel.setText("");
    filenameLabel.setText("-");
    fileSizeLabel.setText("-");
    lastModifiedLabel.setText("-");
    sourceLabel.setText("-");
    idLabel.setText("-");
    repositoryNameLabel.setText("-");
    repositoryTypeLabel.setText("-");

    if (selection.isPresent()) {
      VpaDescriptorRepresentation descriptorRepresentation = selection.get();
      TableManifest manifest = descriptorRepresentation.getManifest();

      filenameLabel.setText(descriptorRepresentation.getFilename());
      fileSizeLabel.setText(descriptorRepresentation.getSize() > 0 ? FileUtils.readableFileSize(descriptorRepresentation.getSize()) : "-");
      lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(descriptorRepresentation.getCreatedAt()));
      sourceLabel.setText(descriptorRepresentation.getSource().getLocation());
      //TODO
//      idLabel.setText(descriptorRepresentation.getManifest().getUuid());
//      idCopyBtn.setVisible(true);
//
//      repositoryTypeLabel.setText(descriptorRepresentation.getSource().getType());
//      repositoryNameLabel.setText(descriptorRepresentation.getSource().getName());
//
//      VpaPackageInfo packageInfo = manifest.getPackageInfo();
//      directb2sIcon.setVisible(packageInfo.isDirectb2s());
//      pupPackIcon.setVisible(packageInfo.isPupPack());
//      romIcon.setVisible(packageInfo.isRom());
//      resIcon.setVisible(packageInfo.isRes());
//      cfgIcon.setVisible(packageInfo.isCfg());
//      popperIcon.setVisible(packageInfo.isPopperMedia());
//      flexIcon.setVisible(packageInfo.isFlexDMD());
//      ultraIcon.setVisible(packageInfo.isUltraDMD());
//      musicIcon.setVisible(packageInfo.isMusic());
//      altSoundIcon.setVisible(packageInfo.isAltSound());
//      altColorIcon.setVisible(packageInfo.isAltColor());
//      povIcon.setVisible(packageInfo.isPov());
//      highscoreIcon.setVisible(packageInfo.isHighscore());
//      highscoreHistoryLabel.setText(String.valueOf(packageInfo.getHighscoreHistoryRecords()));


    }
    else {

    }
  }
}
