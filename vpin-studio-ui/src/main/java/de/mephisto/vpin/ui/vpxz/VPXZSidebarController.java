package de.mephisto.vpin.ui.vpxz;

import de.mephisto.vpin.restclient.vpxz.VPXZDescriptorRepresentation;
import de.mephisto.vpin.restclient.vpxz.VPXZFileInfo;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.vpxz.VPXZPackageInfo;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class VPXZSidebarController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZSidebarController.class);

  @FXML
  private Accordion repositoryAccordion;

  @FXML
  private TitledPane detailsPane;

  @FXML
  private TitledPane assetsPane;

  @FXML
  private Label sourceLabel;

  @FXML
  private Label filenameLabel;

  @FXML
  private Label fileSizeLabel;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label romLabel;

  @FXML
  private Label nvramLabel;

  @FXML
  private Label iniLabel;

  @FXML
  private Label vbsLabel;

  @FXML
  private Label vpxLabel;

  @FXML
  private Label resLabel;

  @FXML
  private Label povLabel;

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
  private Label gameType;

  @FXML
  private Label gameTheme;

  @FXML
  private Label gameVersion;

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
  public void onViewActivated(NavigationOptions options) {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    repositoryAccordion.managedProperty().bindBidirectional(repositoryAccordion.visibleProperty());
    repositoryAccordion.setExpandedPane(manifestPane);
    setVPXZDescriptor(Optional.empty());
  }

  public void setVisible(boolean b) {
    this.repositoryAccordion.setVisible(b);
  }

  public void setVPXZDescriptor(Optional<VPXZDescriptorRepresentation> selection) {
    vpxLabel.setText(applyValue(vpxLabel, null));
    romLabel.setText(applyValue(romLabel, null));
    nvramLabel.setText(applyValue(nvramLabel, null));
    resLabel.setText(applyValue(resLabel, null));
    iniLabel.setText(applyValue(iniLabel, null));
    vbsLabel.setText(applyValue(vbsLabel, null));
    povLabel.setText(applyValue(povLabel, null));

    filenameLabel.setText("-");
    filenameLabel.setTooltip(null);
    fileSizeLabel.setText("-");
    lastModifiedLabel.setText("-");
    sourceLabel.setText("-");
    sourceLabel.setTooltip(null);

    if (selection.isPresent()) {
      VPXZDescriptorRepresentation descriptorRepresentation = selection.get();
      filenameLabel.setText(descriptorRepresentation.getFilename());
      filenameLabel.setTooltip(new Tooltip(descriptorRepresentation.getFilename()));
      fileSizeLabel.setText(descriptorRepresentation.getSize() > 0 ? FileUtils.readableFileSize(descriptorRepresentation.getSize()) : "-");
      lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(descriptorRepresentation.getCreatedAt()));
      sourceLabel.setText(descriptorRepresentation.getSource().getLocation());
      sourceLabel.setTooltip(new Tooltip(descriptorRepresentation.getSource().getLocation()));

      VPXZPackageInfo packageInfo = descriptorRepresentation.getPackageInfo();

      if (packageInfo != null) {
        vpxLabel.setText(applyValue(vpxLabel, packageInfo.getVpx()));
        romLabel.setText(applyValue(romLabel, packageInfo.getRom()));
        nvramLabel.setText(applyValue(nvramLabel, packageInfo.getNvRam()));
        resLabel.setText(applyValue(resLabel, packageInfo.getRes()));
        iniLabel.setText(applyValue(iniLabel, packageInfo.getIni()));
        vbsLabel.setText(applyValue(vbsLabel, packageInfo.getVbs()));
        povLabel.setText(applyValue(povLabel, packageInfo.getPov()));
      }

      TableDetails tableDetails = descriptorRepresentation.getTableDetails();
      manifestPane.setVisible(tableDetails != null);
      if (tableDetails == null) {
        tableDetails = new TableDetails();
      }
      gameName.setText(StringUtils.isEmpty(tableDetails.getGameName()) ? "-" : tableDetails.getGameName());
      gameFileName.setText(StringUtils.isEmpty(tableDetails.getGameFileName()) ? "-" : tableDetails.getGameFileName());
      gameDisplayName.setText(StringUtils.isEmpty(tableDetails.getGameDisplayName()) ? "-" : tableDetails.getGameDisplayName());
      gameYear.setText(tableDetails.getGameYear() == null ? "-" : String.valueOf(tableDetails.getGameYear()));
      romName.setText(StringUtils.isEmpty(tableDetails.getRomName()) ? "-" : tableDetails.getRomName());
      gameType.setText(tableDetails.getGameType() == null ? "-" : tableDetails.getGameType());
      gameVersion.setText(StringUtils.isEmpty(tableDetails.getGameVersion()) ? "-" : tableDetails.getGameVersion());
      gameTheme.setText(StringUtils.isEmpty(tableDetails.getGameTheme()) ? "-" : tableDetails.getGameTheme());
      manufacturer.setText(StringUtils.isEmpty(tableDetails.getManufacturer()) ? "-" : tableDetails.getManufacturer());
      numberOfPlayers.setText(tableDetails.getNumberOfPlayers() == null ? "-" : String.valueOf(tableDetails.getNumberOfPlayers()));
      tags.setText(StringUtils.isEmpty(tableDetails.getTags()) ? "-" : tableDetails.getTags());
      category.setText(StringUtils.isEmpty(tableDetails.getCategory()) ? "-" : tableDetails.getCategory());
      author.setText(StringUtils.isEmpty(tableDetails.getAuthor()) ? "-" : tableDetails.getAuthor());
      launchCustomVar.setText(StringUtils.isEmpty(tableDetails.getLaunchCustomVar()) ? "-" : tableDetails.getLaunchCustomVar());
      keepDisplays.setText(StringUtils.isEmpty(tableDetails.getKeepDisplays()) ? "-" :
          VPinScreen.toString(VPinScreen.keepDisplaysToScreens(tableDetails.getKeepDisplays())));
      gameRating.setText(tableDetails.getGameRating() == null ? "?" : String.valueOf(tableDetails.getGameRating()));
      dof.setText(StringUtils.isEmpty(tableDetails.getDof()) ? "-" : tableDetails.getDof());
      IPDBNum.setText(StringUtils.isEmpty(tableDetails.getIPDBNum()) ? "-" : tableDetails.getIPDBNum());
      altRunMode.setText(StringUtils.isEmpty(tableDetails.getAltRunMode()) ? "-" : tableDetails.getAltRunMode());
      url.setText(StringUtils.isEmpty(tableDetails.getUrl()) ? "-" : tableDetails.getUrl());
      designedBy.setText(StringUtils.isEmpty(tableDetails.getDesignedBy()) ? "-" : tableDetails.getDesignedBy());
      notes.setText(StringUtils.isEmpty(tableDetails.getNotes()) ? "-" : tableDetails.getNotes());
    }
    else {

    }
  }

  private String applyValue(Label label, VPXZFileInfo value) {
    if (value != null) {
      label.setTooltip(new Tooltip(value.toString()));
      return value.toString();
    }
    else {
      label.setTooltip(null);
    }
    return "-";
  }
}
