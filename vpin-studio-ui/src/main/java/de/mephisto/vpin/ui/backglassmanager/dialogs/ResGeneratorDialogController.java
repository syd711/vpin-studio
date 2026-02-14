package de.mephisto.vpin.ui.backglassmanager.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2SFrameType;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.util.ReturnMessage;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.FileSelectorDragEventHandler;
import de.mephisto.vpin.ui.util.FileSelectorDropEventHandler;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import de.mephisto.vpin.ui.util.WaitProgressModel;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.mephisto.vpin.ui.Studio.client;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

public class ResGeneratorDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(ResGeneratorDialogController.class);

  @FXML
  private Node root;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button generateBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private Button fileBtn;

  @FXML
  private StackPane fileNamePane;

  @FXML
  private Label screenResLabel;

  @FXML
  private Label backglassScreenLabel;

  @FXML
  private Label backglassDimensionLabel;

  @FXML
  private Label backglassPositionLabel;

  @FXML
  private Button clearBtn;

  @FXML
  private ImageView previewImage;

  @FXML
  private RadioButton radioStretchBackglass;

  @FXML
  private RadioButton radioCenterBackglass;

  @FXML
  private ComboBox<DirectB2SFrameType> frameTypeCombo;

  @FXML
  private Tooltip frameTypeTooltip;

  @FXML
  private CheckBox turnOnRunAsExe;

  @FXML
  private CheckBox turnOnBackground;

  // The dialog frame
  private Stage stage;

  private int emulatorId;
  private String b2sFileName;
  private int gameId = -1;

  private BufferedImage frameImg;

  private BufferedImage backglassImg;

  private int previewWidth = -1;
  private int previewHeight = -1;

  private File uploadedFrame = null;

  /** As the generation is quite long, keep in a cache the frames when generated */
  private Map<DirectB2SFrameType, BufferedImage> mapFrames = new HashMap<>();

  private boolean stretchedBackglass;

  @FXML
  private void onCancelClick(ActionEvent e) {
    stage.close();
  }

  @FXML
  private void onGenerateClick(ActionEvent event) {
    ProgressResultModel result =  ProgressDialog.createProgressDialog(new WaitProgressModel<>("Generate .res File...", 
      "Generating and saving .res file", 
      () -> {
          DirectB2sScreenRes screenres = client.getBackglassServiceClient().getGlobalScreenRes();
          // Mind that the case where globalRes has a frame background is not fully tested...
          int x = screenres.getFullBackglassX();
          int y = screenres.getFullBackglassY();
          int w = screenres.getFullBackglassWidth();
          int h = screenres.getFullBackglassHeight();

          if (stretchedBackglass) {
            screenres.setBackglassX(x);
            screenres.setBackglassY(y);
            screenres.setBackglassWidth(w);
            screenres.setBackglassHeight(h);

            screenres.setBackgroundX(0);
            screenres.setBackgroundY(0);
            screenres.setBackgroundWidth(0);
            screenres.setBackgroundHeight(0);
            screenres.setBackgroundFilePath(null);
          }
          else {
            if (uploadedFrame != null) {
              String newFrameName = client.getBackglassServiceClient().uploadScreenResFrame(emulatorId, b2sFileName, uploadedFrame);
              if (newFrameName != null) {
                screenres.setBackgroundFilePath(newFrameName);
                uploadedFrame = null;
              }
              else {
                JFXFuture.throwException("Cannot store frame " + uploadedFrame);
              }
            }
            else {
              screenres.setBackgroundFilePath(fileNameField.getText());
            }

            screenres.setFrameType(frameTypeCombo.getValue());

            int backglassFitWidth = w;
            int backglassFitHeight = h;
            // If background is centered, fit new backgroundWidth and backgroundHeight within the screen
            // case where height constraint => add horizontal bezels
            if (backglassImg.getWidth() * backglassFitHeight < backglassImg.getHeight() * backglassFitWidth) {
              backglassFitWidth = (int) ((0.0 + backglassFitHeight) * backglassImg.getWidth() / backglassImg.getHeight());
            }
            else {
              backglassFitHeight = (int) ((0.0 + backglassFitWidth) * backglassImg.getHeight() / backglassImg.getWidth());
            }

            screenres.setBackglassX(x + (w - backglassFitWidth) / 2);
            screenres.setBackglassY(y + (h - backglassFitHeight) / 2);
            screenres.setBackglassWidth(backglassFitWidth);
            screenres.setBackglassHeight(backglassFitHeight);

            // also move the dmd to compensate the move of backglass
            screenres.setDmdX(screenres.getDmdX() - (w - backglassFitWidth) / 2);
            screenres.setDmdY(screenres.getDmdY() - (h - backglassFitHeight) / 2);

            screenres.setBackgroundX(x);
            screenres.setBackgroundY(y);
            screenres.setBackgroundWidth(w);
            screenres.setBackgroundHeight(h);
          }

          screenres.setEmulatorId(emulatorId);
          screenres.setB2SFileName(b2sFileName);
          screenres.setGameId(gameId);

          screenres.setTurnOnRunAsExe(turnOnRunAsExe.isSelected());
          screenres.setTurnOnBackground(turnOnBackground.isSelected());

          return client.getBackglassServiceClient().saveScreenRes(screenres);
        }));

    if (result.isSuccess()) {
      ReturnMessage status = result.getFirstTypedResult();
      if (status != null && !status.isOk()) {
        WidgetFactory.showAlert(stage, "Error", "Error saving .res file :", status.getMessage());
      }
      else {
        stage.close();
        // refresh screens
        if (gameId != -1) {
          EventManager.getInstance().notifyTableChange(gameId, null);
        }
      }
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    this.generateBtn.setDisable(true);

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Frame Picture");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image", "*.jpg", "*.png"));

    File selection = fileChooser.showOpenDialog(stage);
    if (selection != null) {
      this.fileNameField.setText(selection.getAbsolutePath());
      loadFrame(selection);
      refreshPreview();
    }
    else {
      onClear();
    }
  }

  @FXML
  private void onClear() {
    fileNameField.setText("");
    frameImg = null;
    mapFrames.remove(DirectB2SFrameType.USE_FRAME);
    refreshPreview();
  }

  private void loadFrame(File file) {
    try {
      uploadedFrame = file;
      frameImg = ImageIO.read(file);
      mapFrames.put(DirectB2SFrameType.USE_FRAME, frameImg);
    }
    catch (IOException ioe) {
      frameImg = null;
      LOG.error("Cannot load frame " + file.getAbsolutePath(), ioe.getMessage());
    }
  }

  private void refreshPreview() {

    // set disable state for frame options
    DirectB2SFrameType frameType = frameTypeCombo.getValue();
    frameTypeCombo.setDisable(stretchedBackglass);
    fileNamePane.setDisable(stretchedBackglass || !DirectB2SFrameType.USE_FRAME.equals(frameType));
    fileBtn.setDisable(stretchedBackglass || !DirectB2SFrameType.USE_FRAME.equals(frameType));

    if (previewWidth > 0 && previewHeight > 0 && backglassImg != null) {
      BufferedImage preview = new BufferedImage((int) previewWidth, (int) previewHeight, BufferedImage.TYPE_INT_ARGB);

      if (frameImg != null) {
        Image rescaledFrame = frameImg.getScaledInstance(previewWidth, previewHeight, Image.SCALE_DEFAULT);
        preview.getGraphics().drawImage(rescaledFrame, 0, 0, null);
      }

      int backgroundWidth = previewWidth;
      int backgroundHeight = previewHeight;
      if (!stretchedBackglass) {
        // If background is centered, fit new backgroundWidth and backgroundHeight within the preview
        // case where height constraint => add horizontal bezels
        if (backglassImg.getWidth() * previewHeight < backglassImg.getHeight() * previewWidth) {
          backgroundWidth = (int) ((0.0 + previewHeight) * backglassImg.getWidth() / backglassImg.getHeight());
        }
        else {
          backgroundHeight = (int) ((0.0 + previewWidth) * backglassImg.getHeight() / backglassImg.getWidth());
        }
      }
      Image rescaledBackground = backglassImg.getScaledInstance(backgroundWidth, backgroundHeight, Image.SCALE_DEFAULT);
      preview.getGraphics().drawImage(rescaledBackground, (previewWidth - backgroundWidth) / 2, (previewHeight - backgroundHeight) / 2, null);

      // update the preview
      previewImage.setImage(SwingFXUtils.toFXImage(preview, null));
      this.generateBtn.setDisable(false);
    }
    else {
      this.generateBtn.setDisable(true);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.frameImg = null;
    this.mapFrames.clear();
    this.uploadedFrame = null;
    this.generateBtn.setDisable(true);

    // these options are needed to make frames visible, so propose to turn them on autom
    turnOnRunAsExe.setSelected(true);
    turnOnBackground.setSelected(true);

    this.clearBtn.visibleProperty().bind(this.fileNameField.textProperty().isNotEmpty());

    root.setOnDragOver(new FileSelectorDragEventHandler(root, "png", "jpg"));
    root.setOnDragDropped(new FileSelectorDropEventHandler(fileNameField, file -> {
      loadFrame(file);
      refreshPreview();
    }));

    frameTypeCombo.setItems(FXCollections.observableArrayList(DirectB2SFrameType.values()));
    frameTypeCombo.setValue(DirectB2SFrameType.USE_FRAME);
    frameTypeCombo.valueProperty().addListener((obs, ov, nv) -> onGenerateFrame(nv));

    StringBuilder tooltiptext = new StringBuilder();
    tooltiptext.append(frameTypeTooltip.getText()).append("\n");
    for (DirectB2SFrameType type : DirectB2SFrameType.values()) {
      tooltiptext.append("\u2022 ").append(type.getName()).append(": ").append(type.getDescription()).append("\n");
    }
    Label tooltiplbl = new Label(tooltiptext.toString());
    frameTypeTooltip.setText(null);
    frameTypeTooltip.setGraphic(tooltiplbl);

    // create a toggle group 
    ToggleGroup tg = new ToggleGroup();
    radioStretchBackglass.setToggleGroup(tg);
    radioCenterBackglass.setToggleGroup(tg);
    tg.selectedToggleProperty().addListener((obs, o, n) -> {
      this.stretchedBackglass = n == radioStretchBackglass;
      refreshPreview();
    });
    tg.selectToggle(radioStretchBackglass);

    // load screen dimensions
    JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getGlobalScreenRes())
        .thenAcceptLater(screenres -> {
          if (screenres != null) {
            int w = screenres.getFullBackglassWidth();
            int h = screenres.getFullBackglassHeight();

            backglassScreenLabel.setText(formatDimension(w, h));

            // resize the preview proportionnaly
            this.previewHeight = (int) previewImage.getFitHeight();
            double width = previewImage.getFitHeight() * w / h;
            previewImage.setFitWidth(width);
            this.previewWidth = (int) width;
            refreshPreview();
          }
        });
  }

  private void onGenerateFrame(DirectB2SFrameType frameType) {

    if (mapFrames.containsKey(frameType)) {
      frameImg = mapFrames.get(frameType);
      refreshPreview();
      return; 
    }

    ProgressResultModel img =  ProgressDialog.createProgressDialog(new WaitProgressModel<>("Generate Frame...", 
          "Generating a '" + frameType + "' frame", 
          () -> {
            try {
              InputStream in = client.getBackglassServiceClient().generateFrame(emulatorId, b2sFileName, frameType);
              return in != null? ImageIO.read(in): null;
            }
            catch (IOException ioe) {
              LOG.error("Cannot generate background image for backglass {} of emulator {} : {}",
                  b2sFileName, emulatorId, ioe.getMessage());
            }
            return null;
          }));

    if (img.isSuccess()) {
      mapFrames.get(frameType);
      frameImg = img.getFirstTypedResult();
      mapFrames.put(frameType, frameImg);
      refreshPreview();
    }
  }

  @Override
  public void onDialogCancel() {
  }

  public void setData(Stage stage, int emulatorId, String fileName) {
    this.stage = stage;

    JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getScreenRes(emulatorId, fileName, false))
        .thenAcceptLater(res -> setScreenRes(res))
        .onErrorLater(ex -> WidgetFactory.showAlert(stage, "Error", "Cannt load .res file :", ex.getMessage()));

    JFXFuture.supplyAsync(() -> {
          DirectB2SData data = client.getBackglassServiceClient().getDirectB2SData(emulatorId, fileName);
          if (data != null) {
            try {
              // get the preview image without grill if hidden but without frame
              InputStream is = client.getBackglassServiceClient().getDirectB2sPreviewBackground(data, false);
              if (is != null) {
                try {
                  return ImageIO.read(is);
                }
                catch (IOException ioe) {
                  LOG.error("Cannot load background image for backglass {} of emulator {} : {}",
                      fileName, emulatorId, ioe.getMessage());
                }
              }
            }
            catch (IOException ioe) {
              LOG.error("Cannot get background for backglass {} of emulator {} : {}", 
                  fileName, emulatorId, ioe.getMessage());
            }
          }
          return null;
        })
        .thenAcceptLater((img) -> {
          backglassImg = img;
          refreshPreview();
        });
  }

  private void setScreenRes(DirectB2sScreenRes screenres) {

    if (screenres != null) {
      this.emulatorId = screenres.getEmulatorId();
      this.b2sFileName = screenres.getB2SFileName();
      this.gameId = screenres.getGameId();

      screenResLabel.setText(screenres.getScreenresFilePath());
      backglassPositionLabel.setText(formatLocation(screenres.getBackglassX(), screenres.getBackglassY()));
      backglassDimensionLabel.setText(formatDimension(screenres.getBackglassWidth(), screenres.getBackglassHeight()));

      if (screenres.isBackglassCentered()) {
        radioCenterBackglass.setSelected(true);
      }
      else {
        radioStretchBackglass.setSelected(true);
      }
      if (StringUtils.isNotEmpty(screenres.getBackgroundFilePath())) {
        fileNameField.setText(screenres.getBackgroundFilePath());
        JFXFuture.supplyAsync(() -> {
              try {
                return ImageIO.read(client.getBackglassServiceClient().getScreenResFrame(screenres));
              }
              catch (IOException ioe) {
                LOG.error("Cannot load frame image so remove it", ioe);
                return null;
              }
            })
            .thenAcceptLater(img -> {
              frameImg = img;
              mapFrames.put(DirectB2SFrameType.USE_FRAME, img);
              refreshPreview();
            });
      }
    }
    else {
      screenResLabel.setText("No screen res file found, please run B2S_ScreenResIdentifier.exe");
      backglassDimensionLabel.setText("--");
    }
  }

  private String formatLocation(int x, int y) {
    StringBuilder bld = new StringBuilder();
    if (x != -1 || y != -1) {
      bld.append(x).append(" / ").append(y);
    }
    return bld.toString();
  }

  private String formatDimension(int width, int height) {
    StringBuilder bld = new StringBuilder();
    bld.append(width).append(" x ").append(height);
    return bld.toString();
  }
}
