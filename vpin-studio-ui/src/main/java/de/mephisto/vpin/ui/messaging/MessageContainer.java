package de.mephisto.vpin.ui.messaging;

import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;

public class MessageContainer extends BorderPane {

  public static final int VALUE = 600;

  public MessageContainer(JobExecutionResult result) {
    setPrefWidth(VALUE);
    this.getStyleClass().add("custom-menu-item");
    VBox vbox = new VBox();
    vbox.setStyle("-fx-padding: 6 6 6 6;");

    BorderPane top = new BorderPane();
    Label label = new Label("Job Execution Error");
    label.setStyle("-fx-font-weight: bold;");
    top.setLeft(label);

    Button dismissBtn = new Button();
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(12);
    fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
    fontIcon.setIconLiteral("mdi2c-close");
    dismissBtn.setGraphic(fontIcon);
    top.setRight(dismissBtn);

    dismissBtn.setOnAction(new EventHandler<>() {
      @Override
      public void handle(ActionEvent event) {
        Studio.client.getJobsService().dismiss(result.getUuid());
        JobPoller.getInstance().refreshMessagesUI();
      }
    });

    vbox.getChildren().add(top);
    label = new Label(result.getError());
    label.setWrapText(true);
    label.setPrefWidth(VALUE);
    vbox.getChildren().add(label);

    setCenter(vbox);
  }
}
