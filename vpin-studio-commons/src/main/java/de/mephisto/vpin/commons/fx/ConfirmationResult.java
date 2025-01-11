package de.mephisto.vpin.commons.fx;

public class ConfirmationResult {
  private boolean applyClicked;
  private boolean okClicked;
  private boolean checked;
  private boolean cancelClicked;

  public boolean isCancelClicked() {
    return cancelClicked;
  }

  public void setCancelClicked(boolean cancelClicked) {
    this.cancelClicked = cancelClicked;
  }

  public boolean isOkClicked() {
    return okClicked;
  }

  public void setOkClicked(boolean okClicked) {
    this.okClicked = okClicked;
  }

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
