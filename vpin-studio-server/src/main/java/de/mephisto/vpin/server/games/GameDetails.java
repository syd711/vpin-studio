package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "GameDetails")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @CreatedDate
  private Date createdAt;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @LastModifiedDate
  private Date updatedAt;

  @Column(length = 4096)
  public String assets;

  @Column(length = 1024)
  public String updates;

  @Column(name = "extRating", nullable = false, columnDefinition = "integer default 0")
  private int extRating = 0;

  private String notes;

  private String eventLog;

  private Long templateId;

  private String romName;

  private String tableName;

  private String pupPack;

  private String extTableId;

  private String extTableVersionId;

  private String tableVersion;

  private String hsFileName;

  private String ignoredValidations;

  private int pupId;

  private int nvOffset;

  private Boolean cardsDisabled = false;

  private Boolean ignoreUpdates = true;

  private Boolean foundControllerStop = true;

  private Boolean foundTableExit = true;

  private Boolean vrRoomSupport = false;

  private Boolean vrRoomEnabled = false;

  private DMDPackageTypes dmdType;
  private String dmdGameName;
  private String dmdProjectFolder;

  public Boolean getIgnoreUpdates() {
    return ignoreUpdates;
  }

  public void setIgnoreUpdates(Boolean ignoreUpdates) {
    this.ignoreUpdates = ignoreUpdates;
  }

  public Boolean getVrRoomSupport() {
    return vrRoomSupport;
  }

  public void setVrRoomSupport(Boolean vrRoomSupport) {
    this.vrRoomSupport = vrRoomSupport;
  }

  public Boolean getVrRoomEnabled() {
    return vrRoomEnabled;
  }

  public void setVrRoomEnabled(Boolean vrRoomEnabled) {
    this.vrRoomEnabled = vrRoomEnabled;
  }

  public Boolean getFoundControllerStop() {
    return foundControllerStop;
  }

  public void setFoundControllerStop(Boolean foundControllerStop) {
    this.foundControllerStop = foundControllerStop;
  }

  public Boolean getFoundTableExit() {
    return foundTableExit;
  }

  public void setFoundTableExit(Boolean foundTableExit) {
    this.foundTableExit = foundTableExit;
  }

  public Boolean getCardsDisabled() {
    return cardsDisabled;
  }

  public String getEventLog() {
    return eventLog;
  }

  public void setEventLog(String eventLog) {
    this.eventLog = eventLog;
  }

  public Boolean isCardsDisabled() {
    return cardsDisabled;
  }

  public void setCardsDisabled(Boolean cardsDisabled) {
    this.cardsDisabled = cardsDisabled;
  }

  public Long getTemplateId() {
    return templateId;
  }

  public void setTemplateId(Long templateId) {
    this.templateId = templateId;
  }

  public String getPupPack() {
    return pupPack;
  }

  public void setPupPack(String pupPack) {
    this.pupPack = pupPack;
  }

  public String getUpdates() {
    return updates;
  }

  public void setUpdates(String updates) {
    this.updates = updates;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getExtTableId() {
    return extTableId;
  }

  public void setExtTableId(String extTableId) {
    this.extTableId = extTableId;
  }

  public String getExtTableVersionId() {
    return extTableVersionId;
  }

  public void setExtTableVersionId(String extTableVersionId) {
    this.extTableVersionId = extTableVersionId;
  }

  public String getTableVersion() {
    return tableVersion;
  }

  public void setTableVersion(String tableVersion) {
    this.tableVersion = tableVersion;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getAssets() {
    return assets;
  }

  public void setAssets(String assets) {
    this.assets = assets;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public int getPupId() {
    return pupId;
  }

  public void setPupId(int pupId) {
    this.pupId = pupId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(String ignoredValidations) {
    this.ignoredValidations = ignoredValidations;
  }

  @Nullable
  public String getHsFileName() {
    return hsFileName;
  }

  public void setHsFileName(@Nullable String hsFileName) {
    this.hsFileName = hsFileName;
  }

  @Nullable
  public String getRomName() {
    return romName;
  }

  public void setRomName(@Nullable String romName) {
    this.romName = romName;
  }

  public int getNvOffset() {
    return nvOffset;
  }

  public void setNvOffset(int nvOffset) {
    this.nvOffset = nvOffset;
  }

  public int getExtRating() {
    return extRating;
  }

  public void setExtRating(int extRating) {
    this.extRating = extRating;
  }

  public DMDPackageTypes getDMDType() {
    return dmdType;
  }

  public void setDMDType(DMDPackageTypes dmdType) {
    this.dmdType = dmdType;
  }

  public String getDMDGameName() {
    return dmdGameName;
  }

  public void setDMDGameName(String dmdGameName) {
    this.dmdGameName = dmdGameName;
  }

  public String getDMDProjectFolder() {
    return dmdProjectFolder;
  }

  public void setDMDProjectFolder(String dmdProjectFolder) {
    this.dmdProjectFolder = dmdProjectFolder;
  }
}
