open module de.mephisto.vpin.restclient {
  requires org.apache.tomcat.embed.core;
  requires org.slf4j;
  requires spring.web;
  requires com.github.spotbugs.annotations;

  exports de.mephisto.vpin.restclient;
  exports de.mephisto.vpin.restclient.representations;
}