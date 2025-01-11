package de.mephisto.vpin.restclient.games;

public enum CommentType {
  Any, Errors, Todos, Outdated, None;


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
      case None: {
        return "No Comment";
      }
      default: {
        throw new UnsupportedOperationException("Unmapped note type " + this);
      }
    }
  }
}
