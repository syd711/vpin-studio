package de.mephisto.vpin.server.dof;

public enum Trigger {
  TableStart,
  TableExit,
  SystemStart,
  KeyEvent;

  public static String getLabelFor(Trigger trigger) {
    switch (trigger) {
      case TableStart: {
        return "table is started";
      }
      case TableExit: {
        return "table is quit";
      }
      case SystemStart: {
        return "system is started";
      }
      case KeyEvent: {
        return "key is pressed";
      }
    }
    throw new IllegalArgumentException("Invalid trigger value " + trigger);
  }


}
