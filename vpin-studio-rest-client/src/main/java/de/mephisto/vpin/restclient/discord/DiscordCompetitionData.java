package de.mephisto.vpin.restclient.discord;

import de.mephisto.vpin.restclient.util.DateUtil;

import java.util.Date;

public class DiscordCompetitionData {
  private String uuid;
  private String owner;
  private String rom;
  private long fs;
  private Date sdt;
  private Date edt;
  private String name;
  private String tname;
  private long msgId;

  public String getTname() {
    return tname;
  }

  public void setTname(String tname) {
    this.tname = tname;
  }

  public Date getSdt() {
    return sdt;
  }

  public void setSdt(Date sdt) {
    this.sdt = sdt;
  }

  public Date getEdt() {
    return edt;
  }

  public void setEdt(Date edt) {
    this.edt = edt;
  }

  public long getMsgId() {
    return msgId;
  }

  public void setMsgId(long msgId) {
    this.msgId = msgId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public long getFs() {
    return fs;
  }

  public void setFs(long fs) {
    this.fs = fs;
  }

  public boolean isOverlappingWith(Date startSelection, Date endSelection) {
    boolean startOverlap = getSdt().before(endSelection);
    boolean endOverlap = startSelection.before(this.getEdt());
    return startOverlap && endOverlap;
  }

  public boolean isFinished(){
    return DateUtil.endOfToday().before(this.getEdt());
  }
}