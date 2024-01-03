package de.mephisto.vpin.commons.fx.pausemenu.states;

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
