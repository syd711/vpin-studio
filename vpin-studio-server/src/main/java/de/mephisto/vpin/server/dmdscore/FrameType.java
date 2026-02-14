package de.mephisto.vpin.server.dmdscore;

public enum FrameType {
  COLORED_GRAY_2("coloredGray2"),
  COLORED_GRAY_4("coloredGray4"),
  COLORED_GRAY_6("coloredGray6"),
  GRAY_2_PLANES("gray2Planes"),
  GRAY_4_PLANES("gray4Planes"),
  RGB24("rgb24"),
  DIMENSIONS("dimensions"),
  COLOUR("color"),
  PALETTE("palette"),
  CLEAR_COLOUR("clearColor"),
  CLEAR_PALETTE("clearPalette"),
  GAME_NAME("gameName"),
  UNKNOWN("unknown");

  private final String type;

  FrameType(String type) {
      this.type = type;
  }

  public static FrameType getEnum(final String type) {
      if (!type.isEmpty()) {
          for (FrameType frameType : FrameType.values()) {
              if (frameType.type.equals(type)) {
                  return frameType;
              }
          }
      }
      return UNKNOWN;
  }

  @Override
  public String toString() {
    return type;
  }
}