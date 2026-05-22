package de.mephisto.vpin.restclient.players;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.restclient.assets.AssetRepresentation;
import org.jspecify.annotations.NonNull;

import java.time.Instant;

public class PlayerRepresentation {

  private Instant createdAt;

  private Instant updatedAt;

  private long id;

  private AssetRepresentation avatar;

  private String avatarUrl;

  private String initials;

  private String name;

  private String iScoredName;

  private String domain;

  private String duplicatePlayerName;

  private boolean bot;

  private boolean administrative;

  //the uuid of the account on mania.net
  private String maniaAccountUuid;

  @NonNull
  public Account toManiaAccount() {
    Account account = new Account();
    account.setInitials(this.getInitials());
    account.setDisplayName(this.getName());
    account.setUuid(this.getManiaAccountUuid());
    return account;
  }

  @JsonProperty("iScoredName")
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

  public boolean isBot() {
    return bot;
  }

  public void setBot(boolean bot) {
    this.bot = bot;
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

  public AssetRepresentation getAvatar() {
    return avatar;
  }

  public void setAvatar(AssetRepresentation avatar) {
    this.avatar = avatar;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PlayerRepresentation that = (PlayerRepresentation) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }
}
