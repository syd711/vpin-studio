package de.mephisto.vpin.server.preferences;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.assets.Asset;
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

  @OneToOne(cascade = CascadeType.ALL)
  private Asset avatar;

  private String ignoredValidations;

  private String ignoredMedia;

  private String systemName;

  private String resetKey;

  private String overlayKey;

  private String showOverlayOnStartup;

  public String getShowOverlayOnStartup() {
    return showOverlayOnStartup;
  }

  public void setShowOverlayOnStartup(String showOverlayOnStartup) {
    this.showOverlayOnStartup = showOverlayOnStartup;
  }

  public String getResetKey() {
    return resetKey;
  }

  public void setResetKey(String resetKey) {
    this.resetKey = resetKey;
  }

  public String getOverlayKey() {
    return overlayKey;
  }

  public void setOverlayKey(String overlayKey) {
    this.overlayKey = overlayKey;
  }

  public Asset getAvatar() {
    return avatar;
  }

  public void setAvatar(Asset avatar) {
    this.avatar = avatar;
  }

  public String getSystemName() {
    return systemName;
  }

  public void setSystemName(String systemName) {
    this.systemName = systemName;
  }

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

  public String getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(String ignoredValidations) {
    this.ignoredValidations = ignoredValidations;
  }
}
