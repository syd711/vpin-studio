open module de.mephisto.vpin.connectors.vps {
  requires org.slf4j;
  requires java.desktop;
  requires com.fasterxml.jackson.databind;
  requires spring.web;
  requires de.mephisto.vpin.restclient;
  exports de.mephisto.vpin.connectors.mania;
}