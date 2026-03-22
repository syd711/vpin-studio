package de.mephisto.vpin.server.emulators;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "EmulatorDetails")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmulatorDetails {
  @Id
  @Column(name = "emulatorId", nullable = false, unique = true)
  private int emulatorId;

  private String vrLaunchScript;

  private String originalLaunchScript;

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public String getVrLaunchScript() {
    return vrLaunchScript;
  }

  public void setVrLaunchScript(String vrLaunchScript) {
    this.vrLaunchScript = vrLaunchScript;
  }

  public String getOriginalLaunchScript() {
    return originalLaunchScript;
  }

  public void setOriginalLaunchScript(String originalLaunchScript) {
    this.originalLaunchScript = originalLaunchScript;
  }
}
