package de.mephisto.vpin.restclient.games;

public enum CommentType {
  Any, Errors, Todos, Outdated, None;


  @Override
  public String toString() {
      return switch (this) {
          case Any -> "Any";
          case Errors -> "Errors";
          case Todos -> "Todos";
          case Outdated -> "Outdated";
          case None -> "No Comment";
          default -> throw new UnsupportedOperationException("Unmapped note type " + this);
      };
  }
}
