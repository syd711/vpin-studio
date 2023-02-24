package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.VpaManifest;
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

  @FXML
  private Label gameName;

  @FXML
  private Label gameFileName;

  @FXML
  private Label gameDisplayName;

  @FXML
  private Label gameYear;

  @FXML
  private Label romName;

  @FXML
  private Label romUrl;

  @FXML
  private Label manufacturer;

  @FXML
  private Label numberOfPlayers;

  @FXML
  private Label tags;

  @FXML
  private Label category;

  @FXML
  private Label author;

  @FXML
  private Label volume;

  @FXML
  private Label launchCustomVar;

  @FXML
  private Label keepDisplays;

  @FXML
  private Label gameRating;

  @FXML
  private Label dof;

  @FXML
  private Label IPDBNum;

  @FXML
  private Label altRunMode;

  @FXML
  private Label url;

  @FXML
  private Label designedBy;

  @FXML
  private Label notes;

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
    sourceLabel.setText("-");
    idLabel.setText("-");
    repositoryNameLabel.setText("-");
    repositoryTypeLabel.setText("-");

    if (selection.isPresent()) {
      VpaDescriptorRepresentation descriptorRepresentation = selection.get();
      VpaManifest manifest = descriptorRepresentation.getManifest();

      filenameLabel.setText(descriptorRepresentation.getFilename());
      fileSizeLabel.setText(descriptorRepresentation.getSize() > 0 ? FileUtils.readableFileSize(descriptorRepresentation.getSize()) : "-");
      sourceLabel.setText(descriptorRepresentation.getSource().getLocation());
      idLabel.setText(descriptorRepresentation.getManifest().getUuid());
      idCopyBtn.setVisible(true);

      repositoryTypeLabel.setText(descriptorRepresentation.getSource().getType());
      repositoryNameLabel.setText(descriptorRepresentation.getSource().getName());

      VpaPackageInfo packageInfo = manifest.getPackageInfo();
      directb2sIcon.setVisible(packageInfo.isDirectb2s());
      pupPackIcon.setVisible(packageInfo.isPupPack());
      romIcon.setVisible(packageInfo.isRom());
      resIcon.setVisible(packageInfo.isRes());
      cfgIcon.setVisible(packageInfo.isCfg());
      popperIcon.setVisible(packageInfo.isPopperMedia());
      flexIcon.setVisible(packageInfo.isFlexDMD());
      ultraIcon.setVisible(packageInfo.isUltraDMD());
      musicIcon.setVisible(packageInfo.isMusic());
      altSoundIcon.setVisible(packageInfo.isAltSound());
      altColorIcon.setVisible(packageInfo.isAltColor());
      povIcon.setVisible(packageInfo.isPov());
      highscoreIcon.setVisible(packageInfo.isHighscore());
      highscoreHistoryLabel.setText(String.valueOf(packageInfo.getHighscoreHistoryRecords()));


      gameName.setText(StringUtils.isEmpty(manifest.getGameName()) ? "-" : manifest.getGameName());
      gameFileName.setText(StringUtils.isEmpty(manifest.getGameFileName()) ? "-" : manifest.getGameFileName());
      gameDisplayName.setText(StringUtils.isEmpty(manifest.getGameDisplayName()) ? "-" : manifest.getGameDisplayName());
      gameYear.setText(manifest.getGameYear() == 0 ? "-" : String.valueOf(manifest.getGameYear()));
      romName.setText(StringUtils.isEmpty(manifest.getRomName()) ? "-" : manifest.getRomName());
      romUrl.setText(StringUtils.isEmpty(manifest.getRomUrl()) ? "-" : manifest.getRomUrl());
      manufacturer.setText(StringUtils.isEmpty(manifest.getManufacturer()) ? "-" : manifest.getManufacturer());
      numberOfPlayers.setText(manifest.getNumberOfPlayers() == 0 ? "-" : String.valueOf(manifest.getNumberOfPlayers()));
      tags.setText(StringUtils.isEmpty(manifest.getTags()) ? "-" : manifest.getTags());
      category.setText(StringUtils.isEmpty(manifest.getCategory()) ? "-" : manifest.getCategory());
      author.setText(StringUtils.isEmpty(manifest.getAuthor()) ? "-" : manifest.getAuthor());
      volume.setText(manifest.getVolume() > 0 ? String.valueOf(manifest.getVolume() + " %") : "");
      launchCustomVar.setText(StringUtils.isEmpty(manifest.getLaunchCustomVar()) ? "-" : manifest.getLaunchCustomVar());
      keepDisplays.setText(StringUtils.isEmpty(manifest.getKeepDisplays()) ? "-" : manifest.getKeepDisplays());
      gameRating.setText(manifest.getGameRating() > 0 ? String.valueOf(manifest.getGameRating()) : "-");
      dof.setText(StringUtils.isEmpty(manifest.getDof()) ? "-" : manifest.getDof());
      IPDBNum.setText(StringUtils.isEmpty(manifest.getIPDBNum()) ? "-" : manifest.getIPDBNum());
      altRunMode.setText(StringUtils.isEmpty(manifest.getAltRunMode()) ? "-" : manifest.getAltRunMode());
      url.setText(StringUtils.isEmpty(manifest.getUrl()) ? "-" : manifest.getUrl());
      designedBy.setText(StringUtils.isEmpty(manifest.getDesignedBy()) ? "-" : manifest.getDesignedBy());
      notes.setText(StringUtils.isEmpty(manifest.getNotes()) ? "-" : manifest.getNotes());
    }
    else {
      gameName.setText("-");
      gameFileName.setText("-");
      gameDisplayName.setText("-");
      gameYear.setText("-");
      romName.setText("-");
      romUrl.setText("-");
      manufacturer.setText("-");
      numberOfPlayers.setText("-");
      tags.setText("-");
      category.setText("-");
      author.setText("-");
      volume.setText("-");
      launchCustomVar.setText("-");
      keepDisplays.setText("-");
      gameRating.setText("-");
      dof.setText("-");
      IPDBNum.setText("-");
      altRunMode.setText("-");
      url.setText("-");
      designedBy.setText("-");
      notes.setText("-");
    }
  }
}
