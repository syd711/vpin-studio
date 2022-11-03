package de.mephisto.vpin.server.preferences;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Preferences")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Preferences {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  private boolean cardGenerationEnabled;

  private String cardGenerationScreen;

  private String ignoredValidations;

  private String ignoredMedia;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIgnoredMedia() {
    return ignoredMedia;
  }

  public void setIgnoredMedia(String ignoredMedia) {
    this.ignoredMedia = ignoredMedia;
  }

  public boolean isCardGenerationEnabled() {
    return cardGenerationEnabled;
  }

  public void setCardGenerationEnabled(boolean cardGenerationEnabled) {
    this.cardGenerationEnabled = cardGenerationEnabled;
  }

  public String getCardGenerationScreen() {
    return cardGenerationScreen;
  }

  public void setCardGenerationScreen(String cardGenerationScreen) {
    this.cardGenerationScreen = cardGenerationScreen;
  }

  public String getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(String ignoredValidations) {
    this.ignoredValidations = ignoredValidations;
  }
}
