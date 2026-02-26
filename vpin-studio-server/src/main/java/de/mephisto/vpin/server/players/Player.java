package de.mephisto.vpin.server.players;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.assets.Asset;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Players")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Player {

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @CreatedDate
  private Date createdAt;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @LastModifiedDate
  private Date updatedAt;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @OneToOne(cascade = CascadeType.MERGE, optional = true)
  private Asset avatar;

  @JsonInclude()
  @Transient
  private String duplicatePlayerName;

  private String avatarUrl;

  private String initials;

  private String name;

  private String domain;

  private String email;

  @Column(name = "tournamentUserUuid", nullable = true)
  private String maniaAccountUuid;

  @Column(name = "administrative", nullable = false, columnDefinition = "boolean default false")
  private boolean administrative;

  public String getManiaAccountUuid() {
    return maniaAccountUuid;
  }

  public void setManiaAccountUuid(String maniaAccountUuid) {
    this.maniaAccountUuid = maniaAccountUuid;
  }

  public boolean isAdministrative() {
    return administrative;
  }

  public void setAdministrative(boolean administrative) {
    this.administrative = administrative;
  }

  public String getDuplicatePlayerName() {
    return duplicatePlayerName;
  }

  public void setDuplicatePlayerName(String duplicatePlayerName) {
    this.duplicatePlayerName = duplicatePlayerName;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public String getInitials() {
    return initials;
  }

  public void setInitials(String initials) {
    this.initials = initials;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Nullable
  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Asset getAvatar() {
    return avatar;
  }

  public void setAvatar(Asset avatar) {
    this.avatar = avatar;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Player) {
      return this.id.equals(((Player) obj).getId());
    }
    return false;
  }

  @Override
  public String toString() {
    return "Player [" + this.getId() + "/" + this.getInitials() + "]";
  }
}
