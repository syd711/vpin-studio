open module de.mephisto.vpin.restclient {
  requires org.slf4j;
  requires spring.web;
  requires org.apache.commons.lang3;
  requires spring.core;
  requires com.fasterxml.jackson.databind;

  exports de.mephisto.vpin.restclient;
  exports de.mephisto.vpin.restclient.representations;
  exports de.mephisto.vpin.restclient.discord;
  exports de.mephisto.vpin.restclient.util;
  exports de.mephisto.vpin.restclient.descriptors;
  exports de.mephisto.vpin.restclient.client;
  exports de.mephisto.vpin.restclient.jobs;
  exports de.mephisto.vpin.restclient.popper;
  exports de.mephisto.vpin.restclient.mame;
  exports de.mephisto.vpin.restclient.altsound;
}