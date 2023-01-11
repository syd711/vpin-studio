module de.mephisto.vpin.poppermenu {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.web;

  requires org.controlsfx.controls;
  requires org.kordamp.ikonli.javafx;
  requires eu.hansolo.tilesfx;
  requires com.github.spotbugs.annotations;
  requires java.desktop;
  requires org.slf4j;
  requires imgscalr.lib;
  requires org.apache.commons.lang3;
  requires org.apache.commons.io;
  requires com.fasterxml.jackson.databind;

  // add icon pack modules
  requires filters;
  requires javafx.media;
  requires javafx.swing;

  requires de.mephisto.vpin.connectors.discord;
  requires de.mephisto.vpin.restclient;
  requires de.mephisto.vpin.commons;

  exports de.mephisto.vpin.poppermenu;
  opens de.mephisto.vpin.poppermenu to javafx.fxml;
}