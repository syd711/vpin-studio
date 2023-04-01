package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.DirectB2SData;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.MediaUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarDirectB2SController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarDirectB2SController.class);

  @FXML
  private Label nameLabel;

  @FXML
  private Label typeLabel;

  @FXML
  private Label authorLabel;

  @FXML
  private Label artworkLabel;

  @FXML
  private Label grillLabel;

  @FXML
  private Label b2sElementsLabel;

  @FXML
  private Label playersLabel;

  @FXML
  private Label filesizeLabel;

  @FXML
  private Label resolutionLabel;

  @FXML
  private Label modificationDateLabel;

  @FXML
  private ImageView thumbnailImage;

  @FXML
  private Button openDefaultPictureBtn;

  @FXML
  private Button uploadBtn;


  private VPinStudioClient client;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private DirectB2SData data;

  // Add a public no-args constructor
  public TablesSidebarDirectB2SController() {
  }

  @FXML
  private void onUpload() {
    if (game.isPresent()) {
      boolean uploaded = Dialogs.openDirectB2SUploadDialog(game.get());
      if (uploaded) {
        this.tablesSidebarController.getTablesController().onReload();
      }
    }
  }

  @FXML
  private void onOpenDirectB2SBackground() {
    if (game.isPresent() && game.get().isDirectB2SAvailable()) {
      byte[] bytesEncoded = org.apache.commons.codec.binary.Base64.decodeBase64(data.getThumbnailBase64());
      MediaUtil.openMedia(new ByteArrayInputStream(bytesEncoded));
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = Studio.client;
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    openDefaultPictureBtn.setDisable(!g.isPresent() || !g.get().isDirectB2SAvailable());
    uploadBtn.setDisable(!g.isPresent());

    if (g.isPresent() && g.get().isDirectB2SAvailable()) {
      data = client.getDirectB2SData(g.get().getId());
      nameLabel.setText(data.getName());
      typeLabel.setText(this.getTableType(data.getTableType()));
      authorLabel.setText(data.getAuthor());
      artworkLabel.setText(data.getArtwork());
      grillLabel.setText(String.valueOf(data.getGrillHeight()));
      b2sElementsLabel.setText(String.valueOf(data.getB2sElements()));
      playersLabel.setText(String.valueOf(data.getNumberOfPlayers()));
      filesizeLabel.setText(FileUtils.readableFileSize(data.getFilesize()));
      modificationDateLabel.setText(SimpleDateFormat.getDateTimeInstance().format(data.getModificationDate()));

      byte[] bytesEncoded = org.apache.commons.codec.binary.Base64.decodeBase64(data.getThumbnailBase64());
      Image image = new Image(new ByteArrayInputStream(bytesEncoded));
      thumbnailImage.setImage(image);
      resolutionLabel.setText("Resolution: " + (int)image.getWidth() + " x " + (int)image.getHeight());
    }
    else {
      nameLabel.setText("-");
      typeLabel.setText("-");
      authorLabel.setText("-");
      artworkLabel.setText("-");
      b2sElementsLabel.setText("-");
      grillLabel.setText("-");
      playersLabel.setText("-");
      filesizeLabel.setText("-");
      modificationDateLabel.setText("-");
      thumbnailImage.setImage(null);
      resolutionLabel.setText("");
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  private String getTableType(int type) {
    switch (type) {
      case 1: {
        return "Electro Mechanical";
      }
      case 2: {
        return "Solid State Electronic";
      }
      case 3: {
        return "Solid State Electronic with DMD";
      }
      case 4: {
        return "Original";
      }
      default: {
        return "-";
      }
    }
  }
}