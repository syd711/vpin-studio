package de.mephisto.vpin.ui.backglassmanager.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.ReturnMessage;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.FileSelectorDragEventHandler;
import de.mephisto.vpin.ui.util.FileSelectorDropEventHandler;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
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
import java.net.URL;
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
  private Label screenResLabel;

  @FXML
  private Label playfieldDimensionLabel;

  @FXML
  private Label backglassDimensionLabel;

  @FXML
  private Label dmdDimensionLabel;

  @FXML
  private Label dmdPositionLabel;

  @FXML
  private Label playfieldScreenLabel;

  @FXML
  private Label backglassScreenLabel;

  @FXML
  private Label backglassPositionLabel;

  @FXML
  private Label dmdScreenLabel;

  @FXML
  private Button clearBtn;

  @FXML
  private ImageView previewImage;

  @FXML
  private RadioButton radioStretchBackglass;

  @FXML
  private RadioButton radioCenterBackglass;

  @FXML
  private CheckBox turnOnRunAsExe;

  @FXML
  private CheckBox turnOnBackground;

  // The dialog frame
  private Stage stage;

  private DirectB2sScreenRes screenres;

  private BufferedImage frameImg;

  private BufferedImage backglassImg;

  private int previewWidth = -1;
  private int previewHeight = -1;

  private FrontendPlayerDisplay backglassDisplay;

  private File uploadedFrame = null;

  private boolean stretchedBackglass;

  @FXML
  private void onCancelClick(ActionEvent e) {
    stage.close();
  }

  @FXML
  private void onGenerateClick(ActionEvent event) {
    JFXFuture.runAsync(() -> {
          if (stretchedBackglass) {
            screenres.setBackglassX(0);
            screenres.setBackglassY(0);
            screenres.setBackglassWidth(backglassDisplay.getWidth());
            screenres.setBackglassHeight(backglassDisplay.getHeight());

            screenres.setBackgroundX(0);
            screenres.setBackgroundY(0);
            screenres.setBackgroundWidth(0);
            screenres.setBackgroundHeight(0);
            screenres.setBackgroundFilePath(null);
          }
          else {
            if (uploadedFrame != null) {
              String newFrameName = client.getBackglassServiceClient().uploadScreenResFrame(screenres, uploadedFrame);
              if (newFrameName != null) {
                screenres.setBackgroundFilePath(newFrameName);
                uploadedFrame = null;
              }
              else {
                JFXFuture.throwException("Cannot store frame " + uploadedFrame);
              }
            }

            int backglassFitWidth = backglassDisplay.getWidth();
            int backglassFitHeight = backglassDisplay.getHeight();
            // If background is centered, fit new backgroundWidth and backgroundHeight within the screen
            // case where height constraint => add horizontal bezels
            if (backglassImg.getWidth() * backglassFitHeight < backglassImg.getHeight() * backglassFitWidth) {
              backglassFitWidth = (int) ((0.0 + backglassFitHeight) * backglassImg.getWidth() / backglassImg.getHeight());
            }
            else {
              backglassFitHeight = (int) ((0.0 + backglassFitWidth) * backglassImg.getHeight() / backglassImg.getWidth());
            }

            screenres.setBackglassX((backglassDisplay.getWidth() - backglassFitWidth) / 2);
            screenres.setBackglassY((backglassDisplay.getHeight() - backglassFitHeight) / 2);
            screenres.setBackglassWidth(backglassFitWidth);
            screenres.setBackglassHeight(backglassFitHeight);

            screenres.setBackgroundX(0);
            screenres.setBackgroundY(0);
            screenres.setBackgroundWidth(backglassDisplay.getWidth());
            screenres.setBackgroundHeight(backglassDisplay.getHeight());
          }

          screenres.setTurnOnRunAsExe(turnOnRunAsExe.isSelected());
          screenres.setTurnOnBackground(turnOnBackground.isSelected());

          ReturnMessage status = client.getBackglassServiceClient().saveScreenRes(screenres);
          JFXFuture.throwExceptionIfError(status);
        })
        .thenLater(() -> {
          stage.close();
          // refresh screens
          if (screenres.getGameId() != -1) {
            EventManager.getInstance().notifyTableChange(screenres.getGameId(), null);
          }
        })
        .onErrorLater(ex -> {
          WidgetFactory.showAlert(stage, "Error", "Error saving .res file :", ex.getMessage());
        });
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
    refreshPreview();
  }

  private void loadFrame(File file) {
    try {
      uploadedFrame = file;
      frameImg = ImageIO.read(file);
    }
    catch (IOException ioe) {
      frameImg = null;
      LOG.error("Cannot load frame " + file.getAbsolutePath(), ioe.getMessage());
    }
  }

  private void refreshPreview() {

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
    JFXFuture.supplyAsync(() -> client.getFrontendService().getScreenDisplay(VPinScreen.BackGlass))
        .thenAcceptLater(display -> {
          if (display != null) {
            //for (FrontendPlayerDisplay display : displays) {
              //if (VPinScreen.PlayField.equals(display.getScreen())) {
              //  playfieldScreenLabel.setText(formatDimension(display.getWidth(), display.getHeight()));
              //}
              //else if (VPinScreen.BackGlass.equals(display.getScreen())) {
                this.backglassDisplay = display;
                //backglassScreenLabel.setText(formatDimension(display.getWidth(), display.getHeight()));

                // resize the preview proportionnaly
                this.previewHeight = (int) previewImage.getFitHeight();
                double width = previewImage.getFitHeight() * display.getWidth() / display.getHeight();
                previewImage.setFitWidth(width);
                this.previewWidth = (int) width;
              //}
              //else if (VPinScreen.DMD.equals(display.getScreen())) {
              //  dmdScreenLabel.setText(formatDimension(display.getWidth(), display.getHeight()));
              //}
            //}
            refreshPreview();
          }
        });
  }

  @Override
  public void onDialogCancel() {
  }

  public void setData(Stage stage, DirectB2S directB2S) {
    this.stage = stage;

    JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getScreenRes(directB2S, false))
        .thenAcceptLater(res -> setScreenRes(res));

    JFXFuture.supplyAsync(() -> {
          DirectB2SData data = client.getBackglassServiceClient().getDirectB2SData(directB2S);
          if (data != null) {
            try {
              return client.getBackglassServiceClient().getDirectB2sBackground(data);
            }
            catch (IOException ioe) {
              LOG.error("Cannot get background for backglass {} of emulator {} : {}",
                  directB2S.getFileName(), directB2S.getEmulatorId(), ioe.getMessage());
            }
          }
          return null;
        })
        .thenAcceptLater((is) -> {
          if (is != null) {
            try {
              backglassImg = ImageIO.read(is);
            }
            catch (IOException ioe) {
              backglassImg = null;
              LOG.error("Cannot load background image for backglass {} of emulator {} : {}",
                  directB2S.getFileName(), directB2S.getEmulatorId(), ioe.getMessage());
            }
          }
          else {
            backglassImg = null;
          }
          refreshPreview();
        });
  }

  private void setScreenRes(DirectB2sScreenRes res) {
    this.screenres = res;

    if (screenres != null) {
      screenResLabel.setText(screenres.getScreenresFilePath());
      playfieldDimensionLabel.setText(formatDimension(screenres.getPlayfieldWidth(), screenres.getPlayfieldHeight()));
      backglassPositionLabel.setText(formatLocation(screenres.getBackglassX(), screenres.getBackglassY()));
      backglassDimensionLabel.setText(formatDimension(screenres.getBackglassWidth(), screenres.getBackglassHeight()));
      dmdPositionLabel.setText(formatLocation(screenres.getBackglassX() + screenres.getDmdX(), screenres.getBackglassY() + screenres.getDmdY()));
      dmdDimensionLabel.setText(formatDimension(screenres.getDmdWidth(), screenres.getDmdHeight()));

      if (res.isBackglassCentered()) {
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
              refreshPreview();
            });
      }
    }
    else {
      screenResLabel.setText("No screen res file found, please run B2S_ScreenResIdentifier.exe");
      playfieldDimensionLabel.setText("--");
      backglassDimensionLabel.setText("--");
      dmdDimensionLabel.setText("--");
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
