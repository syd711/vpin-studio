package de.mephisto.vpin.ui;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DnDOverlayController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(DnDOverlayController.class);

  @FXML
  private Label messageLabel;

  @FXML
  private Label tableTitleLabel;

  @FXML
  private Label tableLabel;

  @FXML
  private ImageView tableWheelImage;

  @FXML
  private BorderPane root;

  @FXML
  private VBox dropZone;

  protected Parent dndLoadingOverlay;

  private EventHandler<Event> showHandler;
  private EventHandler<Event> hideHandler;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableWheelImage.managedProperty().bindBidirectional(tableWheelImage.visibleProperty());
  }

  public void setMessage(String message) {
    if (message == null) {
      ((VBox) messageLabel.getParent().getParent()).getChildren().clear();
      ;
    }
    else {
      messageLabel.setText(message);
    }
  }

  public void setMessageFontsize(int i) {
    Font font = messageLabel.getFont();
    font = Font.font(font.getFamily(), i);
    messageLabel.setFont(font);

  }

  public void setViewParams(double width, double height) {
    root.setPrefWidth(width);
    root.setPrefHeight(height);

    if (width < 400) {
      dropZone.getStyleClass().clear();
      dropZone.getStyleClass().add("dnd-dashed-border-small");
    }
  }

  public void setGame(@Nullable GameRepresentation game) {
    tableTitleLabel.setVisible(false);
    tableLabel.setVisible(false);
    tableWheelImage.setVisible(false);

    if (game != null) {
      tableTitleLabel.setVisible(true);
      tableLabel.setVisible(true);
      tableWheelImage.setVisible(true);

      FrontendMediaRepresentation frontendMedia = client.getFrontendService().getFrontendMedia(game.getId());
      FrontendMediaItemRepresentation item = frontendMedia.getDefaultMediaItem(VPinScreen.Wheel);
      if (item != null) {
        ByteArrayInputStream gameMediaItem = client.getGameMediaItem(game.getId(), VPinScreen.Wheel);
        tableWheelImage.setImage(new Image(gameMediaItem));
      }
      else {
        tableWheelImage.setImage(new Image(Studio.class.getResourceAsStream("avatar-blank.png")));
      }
      tableLabel.setText("\"" + game.getGameDisplayName() + "\"");
    }
  }

  public static DnDOverlayController load(Pane loaderStack, Node node, boolean singleSelectionOnly) {
    try {
      FXMLLoader loader = new FXMLLoader(DnDOverlayController.class.getResource("overlay-dnd.fxml"));
      Parent dndLoadingOverlay = loader.load();
      DnDOverlayController controller = loader.getController();
      controller.dndLoadingOverlay = dndLoadingOverlay;

      controller.showHandler = new EventHandler<Event>() {
        @Override
        public void handle(Event event) {
          if (!loaderStack.getChildren().contains(dndLoadingOverlay)) {
            node.setVisible(false);
            //dndLoadingOverlay.setTranslateX(node.getTranslateX());
            //dndLoadingOverlay.setTranslateY(node.getTranslateY());
            dndLoadingOverlay.setLayoutX(node.getLayoutX());
            dndLoadingOverlay.setLayoutY(node.getLayoutY());

            Node forDim = node;
            while (!(forDim instanceof Pane)) {
              forDim = forDim.getParent();
            }
            double width = ((Pane) forDim).getWidth();
            double height = ((Pane) forDim).getHeight();
            controller.setViewParams(width, height);
            controller.setGame(null);
            System.out.println("added");
            loaderStack.getChildren().add(dndLoadingOverlay);
            loaderStack.requestLayout();
          }
        }
      };

      controller.hideHandler = new EventHandler<Event>() {
        @Override
        public void handle(Event event) {
          loaderStack.getChildren().remove(dndLoadingOverlay);
          node.setVisible(true);
        }
      };

      dndLoadingOverlay.setOnDragOver(new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
          if (event.getDragboard().hasFiles() && (!singleSelectionOnly || event.getDragboard().getFiles().size() == 1)) {
            event.acceptTransferModes(TransferMode.COPY);
          }
          else if (event.getDragboard().hasContent(DataFormat.URL)) {
            event.acceptTransferModes(TransferMode.COPY);
          }
          else {
            event.consume();
          }
        }
      });

      dndLoadingOverlay.setOnDragExited(new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
          node.setVisible(true);
          System.out.println("removed");
          loaderStack.getChildren().remove(controller.dndLoadingOverlay);
          loaderStack.requestLayout();
        }
      });
      return controller;
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
      return null;
    }
  }

  public void setOnDragDropped(EventHandler<DragEvent> eventHandler) {
    dndLoadingOverlay.setOnDragDropped(eventHandler);
  }

  public void showOverlay() {
    showHandler.handle(null);
  }

  public void hideOverlay() {
    hideHandler.handle(null);
  }
}
