open module de.mephisto.vpin.commons {
  requires org.slf4j;
  requires org.apache.commons.lang3;
  requires com.github.spotbugs.annotations;

  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.web;

  requires org.controlsfx.controls;
  requires eu.hansolo.tilesfx;
  requires java.desktop;

  // add icon pack modules
  requires javafx.media;

  requires de.mephisto.vpin.restclient;

  exports de.mephisto.vpin.commons;
  exports de.mephisto.vpin.commons.fx;
  exports de.mephisto.vpin.commons.fx.widgets;
  exports de.mephisto.vpin.commons.utils;
}