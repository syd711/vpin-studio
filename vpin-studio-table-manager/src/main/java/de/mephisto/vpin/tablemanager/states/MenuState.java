package de.mephisto.vpin.tablemanager.states;

abstract public class MenuState {

  abstract MenuState left();

  abstract MenuState right();

  abstract MenuState enter();

  abstract MenuState back();

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }
}
