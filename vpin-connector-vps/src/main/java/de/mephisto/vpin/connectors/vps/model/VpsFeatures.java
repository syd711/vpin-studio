package de.mephisto.vpin.connectors.vps.model;
public interface VpsFeatures {
  String VPX = "VPX";
  String FP = "FP";
  String FX = "FX";
  String FX2 = "FX2";
  String FX3 = "FX3";

  String INCL_PUP = "incl. PuP";
  String INCL_B2S = "incl. B2S";
  String INCL_ART = "incl. Art";
  String INCL_VIDEO = "incl. Video";

  String MOD = "MOD";
  String NO_ROM = "no ROM";
  String SSF = "SSF";
  String FASTFLIPS = "FastFlips";
  String NFOZZY = "nFozzy";
  String FLEEP = "Fleep";
  String LUT = "LUT";
  String FLEX_DMD = "FlexDMD";
  String HYBRID = "Hybrid";
  String VR = "VR";
  String KIDS = "Kids";
  String ADULT = "Adult";
  String FSS = "FSS";
  String FOUR_K ="4k";
  String SCORBIT ="Scorbit";

  String PUP = "PuP";
  String B2S = "B2S";
  String ROM = "ROM";
  String POV = "POV";
  String MUSIC = "Music";
  String WHEEL = "Wheel";
  String COLOR = "Color";
  String SOUND = "Sound";
  String TOPPER = "Topper";
  String RULES = "Rules";

  String MEDIAPACK = "MediaPack";

  public static String[] forFilter() {
    return new String[] {
      MOD, NO_ROM, SSF, FASTFLIPS, NFOZZY, FLEEP, LUT, HYBRID, VR, KIDS, ADULT, FSS, FOUR_K, SCORBIT, INCL_PUP, MEDIAPACK
    };
  }

}
