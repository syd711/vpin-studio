package de.mephisto.vpin.server.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.dof.DOFCommandExecutor;
import de.mephisto.vpin.server.dof.Trigger;

import javax.persistence.*;

@Entity
@Table(name = "DOFCommands")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DOFCommand {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "pupId", nullable = false)
  private int id;
  private int unit;
  private int portNumber;
  private int value;
  private int durationMs;
  private String trigger;
  private String keyBinding;
  private boolean toggle;
  private String description;
  private boolean toggled;

  public boolean isToggled() {
    return toggled;
  }

  public void setToggled(boolean toggled) {
    this.toggled = toggled;
  }

  public String getDescription() {
    return description;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setUnit(int unit) {
    this.unit = unit;
  }

  public void setPortNumber(int portNumber) {
    this.portNumber = portNumber;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public void setDurationMs(int durationMs) {
    this.durationMs = durationMs;
  }

  public void setTrigger(String trigger) {
    this.trigger = trigger;
  }

  public void setKeyBinding(String keyBinding) {
    this.keyBinding = keyBinding;
  }

  public void setToggle(boolean toggle) {
    this.toggle = toggle;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getId() {
    return id;
  }

  public int getUnit() {
    return unit;
  }

  public int getPortNumber() {
    return portNumber;
  }

  public int getValue() {
    return value;
  }

  public int getDurationMs() {
    return durationMs;
  }

  public String getTrigger() {
    return trigger;
  }

  public String getKeyBinding() {
    return keyBinding;
  }

  public boolean isToggle() {
    return toggle;
  }

  public void execute() {
    DOFCommandExecutor.execute(this);
  }

  @Override
  public String toString() {
    return "DOFCommand " + this.getId() + " [" + this.getUnit() + "|" + this.getPortNumber() + "|" + this.getValue() + "|" + this.getTrigger() + "|" + this.getKeyBinding() + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DOFCommand) {
      return this.id == ((DOFCommand) obj).getId();
    }
    return false;
  }
}
