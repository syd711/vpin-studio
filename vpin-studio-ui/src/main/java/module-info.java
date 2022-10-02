module de.mephisto.vpin.ui {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.web;

  requires org.controlsfx.controls;
  requires com.dlsc.formsfx;
  requires org.kordamp.ikonli.javafx;
  requires org.kordamp.bootstrapfx.core;

  opens de.mephisto.vpin.ui to javafx.fxml;
  exports de.mephisto.vpin.ui;
}