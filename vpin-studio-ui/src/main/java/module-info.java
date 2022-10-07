module de.mephisto.vpin.ui {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.web;

  requires org.controlsfx.controls;
  requires org.kordamp.ikonli.javafx;
  requires eu.hansolo.tilesfx;
  requires java.desktop;
  requires org.slf4j;
  requires imgscalr.lib;
  requires org.apache.commons.lang3;

  // add icon pack modules
  requires org.kordamp.ikonli.materialdesign2;
  requires org.kordamp.ikonli.simplelineicons;
  requires com.github.spotbugs.annotations;
  requires filters;
  requires de.mephisto.vpin.restclient;

  opens de.mephisto.vpin.ui to javafx.fxml;
  exports de.mephisto.vpin.ui;
}