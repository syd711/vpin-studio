package de.mephisto.vpin.restclient.games;

public enum NoteType {
  Any, Errors, Todos, Outdated;


  @Override
  public String toString() {
    switch (this) {
      case Any: {
        return "Any";
      }
      case Errors: {
        return "Errors";
      }
      case Todos: {
        return "Todos";
      }
      case Outdated: {
        return "Outdated";
      }
      default: {
        throw new UnsupportedOperationException("Unmapped note type " + this);
      }
    }
  }
}
