package de.mephisto.vpin.restclient.discord;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.OffsetDateTime;

public class DiscordCompetitionData {
  private String uuid;
  private String owner;
  private String rom;
  private long fs;
  private OffsetDateTime sdt;
  private OffsetDateTime edt;
  private String name;
  private String tname;
  private String mode;
  private long msgId;
  private int scrL;
  private String chksm;

  public String getChksm() {
    return chksm;
  }

  public void setChksm(String chksm) {
    this.chksm = chksm;
  }

  public int getScrL() {
    return scrL;
  }

  public void setScrL(int scrL) {
    this.scrL = scrL;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getTname() {
    return tname;
  }

  public void setTname(String tname) {
    this.tname = tname;
  }

  public OffsetDateTime getSdt() {
    return sdt;
  }

  public void setSdt(OffsetDateTime sdt) {
    this.sdt = sdt;
  }

  public OffsetDateTime getEdt() {
    return edt;
  }

  public void setEdt(OffsetDateTime edt) {
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

  public boolean isOverlappingWith(OffsetDateTime startSelection, OffsetDateTime endSelection) {
    boolean startOverlap = getSdt().isBefore(endSelection);
    boolean endOverlap = startSelection.isBefore(this.getEdt());
    return startOverlap && endOverlap;
  }

  @JsonIgnore
  public boolean isFinished() {
    return OffsetDateTime.now().isBefore(this.getEdt());
  }
}