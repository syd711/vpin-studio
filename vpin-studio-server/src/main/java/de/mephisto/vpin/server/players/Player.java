package de.mephisto.vpin.server.players;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.assets.Asset;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "Players")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Player {

  @Column(nullable = false, updatable = false)
  @CreatedDate
  private Instant createdAt;

  @Column(nullable = false)
  @LastModifiedDate
  private Instant updatedAt;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(cascade = CascadeType.MERGE, optional = true)
  private Asset avatar;

  @JsonInclude()
  @Transient
  private String duplicatePlayerName;

  private String avatarUrl;

  private String initials;

  private String name;

  private String iScoredName;

  private String domain;

  private String email;

  @Column(name = "tournamentUserUuid", nullable = true)
  private String maniaAccountUuid;

  @Column(name = "administrative", nullable = false, columnDefinition = "boolean default false")
  private boolean administrative;

  public String getiScoredName() {
    return iScoredName;
  }

  public void setiScoredName(String iScoredName) {
    this.iScoredName = iScoredName;
  }

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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Player player = (Player) o;
    return Objects.equals(id, player.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Player [" + this.getId() + "/" + this.getInitials() + "]";
  }
}
