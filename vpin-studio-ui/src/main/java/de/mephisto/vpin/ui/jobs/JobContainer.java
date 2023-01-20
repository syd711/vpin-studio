package de.mephisto.vpin.ui.jobs;

import de.mephisto.vpin.restclient.JobDescriptor;
import de.mephisto.vpin.ui.Studio;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class JobContainer extends BorderPane {

  public JobContainer(JobDescriptor job) {
    setPrefWidth(300);
    setPrefHeight(46);
    setPadding(new Insets(3,3,3,3));

//    if(job.getImageUrl() == null) {
      ProgressIndicator p = new ProgressIndicator();
      p.setPrefWidth(56);
      p.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
      setLeft(p);
//    }
//    else {
//      ImageView imageView = new ImageView();
//      imageView.setPreserveRatio(true);
//
//      Image image = new Image(Studio.client.getURL(job.getImageUrl()));
//      imageView.setImage(image);
//    }

    VBox vbox = new VBox(3);
    Label label = new Label(job.getTitle());
    vbox.getChildren().add(label);
    label = new Label(job.getDescription());
    vbox.getChildren().add(label);

    setCenter(vbox);
  }
}
