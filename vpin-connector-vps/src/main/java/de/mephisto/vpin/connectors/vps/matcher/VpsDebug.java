package de.mephisto.vpin.connectors.vps.matcher;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

public class VpsDebug {

  private StringBuilder debug = new StringBuilder();

  private DecimalFormat decimalFormat = new DecimalFormat("0.00");

  public void clear() {
    debug.setLength(0);
  }

  public void startDebug() {
    debug.append("\n | ");
  }

  public void appendDebug(double d, int size) {
    debug.append(StringUtils.leftPad(decimalFormat.format(d), size))
      .append(" | ");
  }
  
  public void appendDebug(String txt, int size) {
    debug.append(StringUtils.rightPad(txt != null ? StringUtils.abbreviate(txt, size): "", size))
      .append(" | ");
  }

  public void endDebug() {
  }

  public String toString() {
    return debug.toString();
  }
}
