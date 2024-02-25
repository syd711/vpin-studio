open module de.mephisto.vpin.restclient {
  requires org.slf4j;
  requires spring.web;
  requires org.apache.commons.lang3;
  requires spring.core;
  requires com.fasterxml.jackson.databind;
  requires org.apache.commons.io;

  requires de.mephisto.vpin.connectors.assets;
  requires de.mephisto.vpin.connectors.mania;
  requires de.mephisto.vpin.connectors.vps;

  exports de.mephisto.vpin.restclient;
  exports de.mephisto.vpin.restclient.alx;
  exports de.mephisto.vpin.restclient.representations;
  exports de.mephisto.vpin.restclient.discord;
  exports de.mephisto.vpin.restclient.util;
  exports de.mephisto.vpin.restclient.games.descriptors;
  exports de.mephisto.vpin.restclient.client;
  exports de.mephisto.vpin.restclient.jobs;
  exports de.mephisto.vpin.restclient.popper;
  exports de.mephisto.vpin.restclient.mame;
  exports de.mephisto.vpin.restclient.altsound;
  exports de.mephisto.vpin.restclient.vpx;
  exports de.mephisto.vpin.restclient.altcolor;
  exports de.mephisto.vpin.restclient.archiving;
  exports de.mephisto.vpin.restclient.assets;
  exports de.mephisto.vpin.restclient.competitions;
  exports de.mephisto.vpin.restclient.directb2s;
  exports de.mephisto.vpin.restclient.highscores;
  exports de.mephisto.vpin.restclient.games;
  exports de.mephisto.vpin.restclient.puppacks;
  exports de.mephisto.vpin.restclient.players;
  exports de.mephisto.vpin.restclient.validation;
  exports de.mephisto.vpin.restclient.vpbm;
  exports de.mephisto.vpin.restclient.system;
  exports de.mephisto.vpin.restclient.util.ini;
  exports de.mephisto.vpin.restclient.util.properties;
  exports de.mephisto.vpin.restclient.dmd;
  exports de.mephisto.vpin.restclient.components;
  exports de.mephisto.vpin.restclient.cards;
  exports de.mephisto.vpin.restclient.dof;
  exports de.mephisto.vpin.restclient.tournaments;
  exports de.mephisto.vpin.restclient.preferences;
  exports de.mephisto.vpin.restclient.vps;
  exports de.mephisto.vpin.restclient.textedit;
}