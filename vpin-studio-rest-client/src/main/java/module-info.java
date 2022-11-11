open module de.mephisto.vpin.restclient {
  requires org.slf4j;
  requires spring.web;
  requires org.apache.commons.lang3;
  requires spring.core;
  requires com.fasterxml.jackson.databind;

  exports de.mephisto.vpin.restclient;
  exports de.mephisto.vpin.restclient.representations;
}