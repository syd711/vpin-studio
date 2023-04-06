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

  public String toCSV() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.id);
    builder.append(",");
    return builder.toString();
  }
}
