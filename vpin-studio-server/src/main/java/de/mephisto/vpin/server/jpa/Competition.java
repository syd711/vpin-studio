
package de.mephisto.vpin.server.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "Competitions")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Competition {

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @CreatedDate
  private Date createdAt;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @LastModifiedDate
  private Date updatedAt;

  @Id
  @Column(name = "id", nullable = false)
  private Long id;

  private Long gameId;

  private String type;

  @OneToMany
  private Set<Highscore> highscores;

  @OneToOne(cascade = CascadeType.ALL)
  private Asset logo;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Asset getLogo() {
    return logo;
  }


  public void setLogo(Asset logo) {
    this.logo = logo;
  }

  private Date startDate;

  private Date endDate;

  public Long getGameId() {
    return gameId;
  }

  public void setGameId(Long gameId) {
    this.gameId = gameId;
  }

  public Set<Highscore> getHighscores() {
    return highscores;
  }

  public void setHighscores(Set<Highscore> highscores) {
    this.highscores = highscores;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
}
