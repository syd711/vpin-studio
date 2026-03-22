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
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false, unique = true)
  private Long id;

  private int emulatorId;

  private String vrLaunchScript;

  private String originalLaunchScript;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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
