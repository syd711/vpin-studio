package de.mephisto.vpin.ui.messaging;

import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.net.URI;

public class MessageContainer extends BorderPane {
  private final static Logger LOG = LoggerFactory.getLogger(MessageContainer.class);

  public static final int VALUE = 600;

  public MessageContainer(JobExecutionResult result) {
    setPrefWidth(VALUE);
    this.getStyleClass().add("custom-menu-item");
    VBox vbox = new VBox();
    vbox.setStyle("-fx-padding: 6 6 6 6;");

    BorderPane borderPane = new BorderPane();
    Label label = new Label("Job Execution Error");
    if (result.getError() == null) {
      label.setText(result.getMessage());
    }
    label.setStyle("-fx-font-weight: bold;");
    VBox centerBox = new VBox();
    centerBox.setStyle("-fx-padding: 0 6 0 6;");
    centerBox.setAlignment(Pos.TOP_LEFT);
    centerBox.getChildren().add(label);
    centerBox.setSpacing(6);
    borderPane.setCenter(centerBox);

    if (result.getImgUrl() != null && !result.getImgUrl().endsWith(".webp")) {
      Image image = new Image(result.getImgUrl());
      ImageView imageView = new ImageView(image);
      imageView.setPreserveRatio(false);
      imageView.setFitWidth(150);
      borderPane.setLeft(imageView);
    }
    else if(result.getGameId() > 0) {
      Image image;
      GameRepresentation game = Studio.client.getGameService().getGame(result.getGameId());
      FrontendMediaItemRepresentation item = game.getGameMedia().getDefaultMediaItem(VPinScreen.Wheel);
      if (item == null) {
        image = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
      }
      else {
        ByteArrayInputStream gameMediaItem = Studio.client.getGameMediaItem(game.getId(), VPinScreen.Wheel);
        image = new Image(gameMediaItem);
      }

      ImageView imageView = new ImageView(image);
      imageView.setPreserveRatio(true);
      imageView.setFitWidth(100);
      borderPane.setLeft(imageView);
    }

    if(result.getExternalUrl() != null) {
      HBox hBox = new HBox();
      hBox.setAlignment(Pos.CENTER_LEFT);

      Button openBtn = new Button("Open");
      openBtn.setStyle("-fx-font-size: 14px;");
      openBtn.getStyleClass().add("external-component");
      FontIcon fontIcon = new FontIcon();
      fontIcon.setIconSize(12);
      fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
      fontIcon.setIconLiteral("mdi2o-open-in-new");
      openBtn.setGraphic(fontIcon);
      openBtn.setOnAction(new EventHandler<>() {
        @Override
        public void handle(ActionEvent event) {
          try {
            Desktop.getDesktop().browse(new URI(result.getExternalUrl()));
          } catch (Exception ex) {
            LOG.error("Failed to open link: " + ex.getMessage(), ex);
          }
        }
      });
      hBox.getChildren().add(openBtn);
      centerBox.getChildren().add(hBox);
    }

    Button dismissBtn = new Button();
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(12);
    fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
    fontIcon.setIconLiteral("mdi2c-close");
    dismissBtn.setGraphic(fontIcon);
    borderPane.setRight(dismissBtn);

    dismissBtn.setOnAction(new EventHandler<>() {
      @Override
      public void handle(ActionEvent event) {
        Studio.client.getJobsService().dismiss(result.getUuid());
        JobPoller.getInstance().refreshMessagesUI(true);
      }
    });

    vbox.getChildren().add(borderPane);
    label = new Label(result.getError());
    label.setWrapText(true);
    label.setPrefWidth(VALUE);
    vbox.getChildren().add(label);

    setCenter(vbox);
  }
}
