module de.mephisto.vpin.ui {
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
  requires org.apache.commons.codec;
  requires com.fasterxml.jackson.databind;

  // add icon pack modules
  requires org.kordamp.ikonli.materialdesign2;
  requires org.kordamp.ikonli.simplelineicons;
  requires filters;
  requires javafx.media;
  requires javafx.swing;

  requires de.mephisto.vpin.connectors.discord;
  requires de.mephisto.vpin.restclient;
  requires de.mephisto.vpin.commons;

  opens de.mephisto.vpin.ui to javafx.fxml;
  exports de.mephisto.vpin.ui;
  exports de.mephisto.vpin.ui.cards;
  opens de.mephisto.vpin.ui.cards to javafx.fxml;
  exports de.mephisto.vpin.ui.events;
  opens de.mephisto.vpin.ui.events to javafx.fxml;
  exports de.mephisto.vpin.ui.tables;
  opens de.mephisto.vpin.ui.tables to javafx.fxml;
  exports de.mephisto.vpin.ui.competitions;
  opens de.mephisto.vpin.ui.competitions to javafx.fxml;
  exports de.mephisto.vpin.ui.players;
  opens de.mephisto.vpin.ui.players to javafx.fxml;
  exports de.mephisto.vpin.ui.preferences;
  opens de.mephisto.vpin.ui.preferences to javafx.fxml;
  exports de.mephisto.vpin.ui.tables.dialogs;
  opens de.mephisto.vpin.ui.tables.dialogs to javafx.fxml;
  exports de.mephisto.vpin.ui.launcher;
  opens de.mephisto.vpin.ui.launcher to javafx.fxml;
  exports de.mephisto.vpin.ui.util;
  opens de.mephisto.vpin.ui.util to javafx.fxml;
  exports de.mephisto.vpin.ui.archiving;
  opens de.mephisto.vpin.ui.archiving to javafx.fxml;
  exports de.mephisto.vpin.ui.archiving.dialogs;
  opens de.mephisto.vpin.ui.archiving.dialogs to javafx.fxml;
}