package de.mephisto.vpin.restclient.altsound;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ID,CHANNEL,DUCK,GAIN,LOOP,STOP,NAME,FNAME,GROUP,SHAKER,SERIAL,PRELOAD,STOPCMD
 * 0x0002,0,100,85,100,0,"normal_prelaunch","0x0002-normal_prelaunch.ogg",1,,,0,
 */
public class AltSoundEntry {
  public static final String ID = "ID";
  public static final String CHANNEL = "CHANNEL";
  public static final String DUCK = "DUCK";
  public static final String GAIN = "GAIN";
  public static final String LOOP = "LOOP";
  public static final String STOP = "STOP";
  public static final String NAME = "NAME";
  public static final String FNAME = "FNAME";
  public static final String GROUP = "GROUP";
  public static final String SHAKER = "SHAKER";
  public static final String SERIAL = "SERIAL";
  public static final String PRELOAD = "PRELOAD";
  public static final String STOPCMD = "STOPCMD";

  public static final List<String> DEFAULT_HEADERS = List.of(ID, CHANNEL, DUCK, GAIN, LOOP, STOP, NAME, FNAME, GROUP, SHAKER, SERIAL, PRELOAD, STOPCMD);

  private String id;
  private String channel;
  private int duck;
  private int gain;
  private int loop;
  private int stop;
  private String name;
  private String filename;
  private int group;
  private String shaker;
  private String serial;
  private int preload;
  private String stopCmd;
  private boolean exists;
  private long size;

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public boolean isExists() {
    return exists;
  }

  public void setExists(boolean exists) {
    this.exists = exists;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public int getDuck() {
    return duck;
  }

  public void setDuck(int duck) {
    this.duck = duck;
  }

  public int getGain() {
    return gain;
  }

  public void setGain(int gain) {
    this.gain = gain;
  }

  public int getLoop() {
    return loop;
  }

  public void setLoop(int loop) {
    this.loop = loop;
  }

  public int getStop() {
    return stop;
  }

  public void setStop(int stop) {
    this.stop = stop;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public int getGroup() {
    return group;
  }

  public void setGroup(int group) {
    this.group = group;
  }

  public String getShaker() {
    return shaker;
  }

  public void setShaker(String shaker) {
    this.shaker = shaker;
  }

  public String getSerial() {
    return serial;
  }

  public void setSerial(String serial) {
    this.serial = serial;
  }

  public int getPreload() {
    return preload;
  }

  public void setPreload(int preload) {
    this.preload = preload;
  }

  public String getStopCmd() {
    return stopCmd;
  }

  public void setStopCmd(String stopCmd) {
    this.stopCmd = stopCmd;
  }

  public String toCSV(AltSound altSound) {
    List<String> headers = altSound.getHeaders().isEmpty() ? DEFAULT_HEADERS : altSound.getHeaders();
    List<String> values = new ArrayList<>();
    for (String header : headers) {
      values.add(getCSVValue(header));
    }
    return String.join(",", values);
  }

  /**
   * The values are written in the column order of the CSV header, unknown columns are left empty.
   */
  private String getCSVValue(String header) {
    switch (header.trim().toUpperCase()) {
      case ID:
        return quote(this.id);
      case CHANNEL:
        return StringUtils.defaultString(this.channel);
      case DUCK:
        return String.valueOf(this.duck);
      case GAIN:
        return String.valueOf(this.gain);
      case LOOP:
        return String.valueOf(this.loop);
      case STOP:
        return String.valueOf(this.stop);
      case NAME:
        return quote(this.name);
      case FNAME:
        return quote(this.filename);
      case GROUP:
        return String.valueOf(this.group);
      case SHAKER:
        return quoteIfRequired(this.shaker);
      case SERIAL:
        return quoteIfRequired(this.serial);
      case PRELOAD:
        return String.valueOf(this.preload);
      case STOPCMD:
        return quoteIfRequired(this.stopCmd);
      default:
        return "";
    }
  }

  private static String quote(String value) {
    return "\"" + StringUtils.defaultString(value).replace("\"", "\"\"") + "\"";
  }

  private static String quoteIfRequired(String value) {
    if (value != null && (value.contains(",") || value.contains("\""))) {
      return quote(value);
    }
    return StringUtils.defaultString(value);
  }

  public AltSoundEntry copy() {
    AltSoundEntry copy = new AltSoundEntry();
    copy.id = this.id;
    copy.channel = this.channel;
    copy.duck = this.duck;
    copy.gain = this.gain;
    copy.loop = this.loop;
    copy.stop = this.stop;
    copy.name = this.name;
    copy.filename = this.filename;
    copy.group = this.group;
    copy.shaker = this.shaker;
    copy.serial = this.serial;
    copy.preload = this.preload;
    copy.stopCmd = this.stopCmd;
    copy.exists = this.exists;
    copy.size = this.size;
    return copy;
  }

  public String toGSoundCSV(AltSound altSound) {
    StringBuilder builder = new StringBuilder();
    builder.append(this.id);
    builder.append(",");
    builder.append(StringUtils.isEmpty(this.channel) ? "" : this.channel);
    builder.append(",");
    builder.append(this.gain);
    builder.append(",");
    builder.append(this.duck);
    builder.append(",");
    builder.append(this.filename);
    return builder.toString();
  }
}
