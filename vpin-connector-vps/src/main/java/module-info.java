open module de.mephisto.vpin.connectors.vps {
  requires org.slf4j;
  requires java.desktop;
  requires com.fasterxml.jackson.databind;
  requires logback.core;
  exports de.mephisto.vpin.connectors.vps;
  exports de.mephisto.vpin.connectors.vps.model;
}