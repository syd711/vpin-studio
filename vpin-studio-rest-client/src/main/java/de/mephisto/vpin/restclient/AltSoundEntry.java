package de.mephisto.vpin.restclient;

/**
 * ID,CHANNEL,DUCK,GAIN,LOOP,STOP,NAME,FNAME,GROUP,SHAKER,SERIAL,PRELOAD,STOPCMD
 * 0x0002,0,100,85,100,0,"normal_prelaunch","0x0002-normal_prelaunch.ogg",1,,,0,
 */
public class AltSoundEntry {
  private String id;
  private int channel;
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

  public int getChannel() {
    return channel;
  }

  public void setChannel(int channel) {
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
    StringBuilder builder = new StringBuilder();
    builder.append("\"");
    builder.append(this.id);
    builder.append("\",");
    builder.append(this.channel);
    builder.append(",");
    builder.append(this.duck);
    builder.append(",");
    builder.append(this.gain);
    builder.append(",");
    builder.append(this.loop);
    builder.append(",");
    builder.append(this.stop);
    builder.append(",\"");
    builder.append(this.name);
    builder.append("\",\"");
    builder.append(this.filename);
    builder.append("\"");

    if (altSound.getHeaders().contains("GROUP")) {
      builder.append(",");
      builder.append(this.group);
    }

    if (altSound.getHeaders().contains("SHAKER")) {
      builder.append(",");
      builder.append(this.shaker != null ? this.shaker : "");
    }

    if (altSound.getHeaders().contains("SERIAL")) {
      builder.append(",");
      builder.append(this.serial != null ? this.serial : "");
    }

    if (altSound.getHeaders().contains("PRELOAD")) {
      builder.append(",");
      builder.append(this.preload);
    }

    if (altSound.getHeaders().contains("STOPCMD")) {
      builder.append(",");
      builder.append(this.stopCmd != null ? this.stopCmd : "");
    }
    return builder.toString();
  }
}
