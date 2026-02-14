package de.mephisto.vpin.commons.fx.cards;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class LogTime {

  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private String name;
  private long startTime;
  private long endTime;

  public LogTime(String name) {
    this.name = name;
    this.startTime = System.currentTimeMillis();
  }

  public void pulse(String pulseName) {
    endTime = System.currentTimeMillis();
    LOG.debug(name + "/" + pulseName + ": " + (endTime - startTime) + " ms");
    startTime = endTime;
  }

}
