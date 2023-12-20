package de.mephisto.vpin.server.mania;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "ManiaAccount")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManiaAccount {

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @CreatedDate
  private Date createdAt;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @LastModifiedDate
  private Date updatedAt;

  @Id
  private String cabinetId;

  private String uuid;
  private String initials;
  private String displayName;

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

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getCabinetId() {
    return cabinetId;
  }

  public void setCabinetId(String cabinetId) {
    this.cabinetId = cabinetId;
  }

  public String getInitials() {
    return initials;
  }

  public void setInitials(String initials) {
    this.initials = initials;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ManiaAccount)) return false;

    ManiaAccount that = (ManiaAccount) o;

    return Objects.equals(cabinetId, that.cabinetId);
  }

  @Override
  public int hashCode() {
    return cabinetId != null ? cabinetId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "VPin-Mania Account '" + this.cabinetId + "'";
  }
}
