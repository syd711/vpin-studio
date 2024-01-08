package de.mephisto.vpin.commons.fx;

public class ConfirmationResult {
  private boolean applyClicked;
  private boolean checked;

  public boolean isApplyClicked() {
    return applyClicked;
  }

  public void setApplyClicked(boolean applyClicked) {
    this.applyClicked = applyClicked;
  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
  }
}
