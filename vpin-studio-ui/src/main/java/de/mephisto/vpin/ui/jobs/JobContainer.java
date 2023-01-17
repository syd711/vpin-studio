package de.mephisto.vpin.ui.jobs;

import de.mephisto.vpin.restclient.JobDescriptor;
import javafx.geometry.Insets;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class JobContainer extends BorderPane {

  public JobContainer(JobDescriptor job) {

    HBox hbox = new HBox();
    hbox.setPrefWidth(300);
    hbox.setPrefHeight(46);
    hbox.setSpacing(3);
    hbox.setPadding(new Insets(3,3,3,3));

    ProgressIndicator p = new ProgressIndicator();
    p.setPrefWidth(56);
    p.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    hbox.getChildren().add(p);

    VBox vbox = new VBox(3);
    Label label = new Label("Export Job");
    vbox.getChildren().add(label);
    label = new Label("ldalk ajasasdlök lka df df");
    vbox.getChildren().add(label);
    hbox.getChildren().add(vbox);
    CustomMenuItem item = new CustomMenuItem();
    item.setContent(hbox);
//    jobBtn.getItems().add(item);
  }
}
