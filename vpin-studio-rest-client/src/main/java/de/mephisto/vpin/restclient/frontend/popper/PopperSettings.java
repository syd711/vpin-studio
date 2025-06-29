package de.mephisto.vpin.restclient.frontend.popper;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopperSettings extends JsonSettings {
  private final static Logger LOG = LoggerFactory.getLogger(PopperSettings.class);

  private int delayReturn = 2000;
  private boolean returnNext = false;
  private boolean noSysFavs = false;
  private boolean noSysLists = false;
  private int emuExitCount = 1;
  private boolean playOnlyMode = false;
  private int wheelAniTimeMS = 300;
  private boolean showInfoInGame = false;
  private boolean popUPHideAnykey = false;
  private int rapidFireCount = 6;
  private boolean pauseOnLoad = false;
  private boolean pauseOnLoadPF = true;
  private int autoExitEmuSeconds = 0;
  private int introSkipSeconds = 0;
  private boolean attractOnStart = false;
  private boolean muteLaunchAudio = false;
  private int wheelUpdateMS = 30;
  private int fadeoutLoading = 0;
  private int launchTimeoutSecs = 60;
  private boolean joyAxisMove = false;
  private boolean volumeChange = true;
  private boolean useAltWheels = false;
  private boolean watchDog;

  public boolean isWatchDog() {
    return watchDog;
  }

  public void setWatchDog(boolean watchDog) {
    this.watchDog = watchDog;
  }

  public boolean isUseAltWheels() {
    return useAltWheels;
  }

  public void setUseAltWheels(boolean useAltWheels) {
    this.useAltWheels = useAltWheels;
  }

  public int getDelayReturn() {
    return delayReturn;
  }

  public void setDelayReturn(int delayReturn) {
    this.delayReturn = delayReturn;
  }

  public boolean isReturnNext() {
    return returnNext;
  }

  public void setReturnNext(boolean returnNext) {
    this.returnNext = returnNext;
  }

  public boolean isNoSysFavs() {
    return noSysFavs;
  }

  public void setNoSysFavs(boolean noSysFavs) {
    this.noSysFavs = noSysFavs;
  }

  public boolean isNoSysLists() {
    return noSysLists;
  }

  public void setNoSysLists(boolean noSysLists) {
    this.noSysLists = noSysLists;
  }

  public int getEmuExitCount() {
    return emuExitCount;
  }

  public void setEmuExitCount(int emuExitCount) {
    this.emuExitCount = emuExitCount;
  }

  public boolean isPlayOnlyMode() {
    return playOnlyMode;
  }

  public void setPlayOnlyMode(boolean playOnlyMode) {
    this.playOnlyMode = playOnlyMode;
  }

  public int getWheelAniTimeMS() {
    return wheelAniTimeMS;
  }

  public void setWheelAniTimeMS(int wheelAniTimeMS) {
    this.wheelAniTimeMS = wheelAniTimeMS;
  }

  public boolean isShowInfoInGame() {
    return showInfoInGame;
  }

  public void setShowInfoInGame(boolean showInfoInGame) {
    this.showInfoInGame = showInfoInGame;
  }

  public boolean isPopUPHideAnykey() {
    return popUPHideAnykey;
  }

  public void setPopUPHideAnykey(boolean popUPHideAnykey) {
    this.popUPHideAnykey = popUPHideAnykey;
  }

  public int getRapidFireCount() {
    return rapidFireCount;
  }

  public void setRapidFireCount(int rapidFireCount) {
    this.rapidFireCount = rapidFireCount;
  }

  public boolean isPauseOnLoad() {
    return pauseOnLoad;
  }

  public void setPauseOnLoad(boolean pauseOnLoad) {
    this.pauseOnLoad = pauseOnLoad;
  }

  public boolean isPauseOnLoadPF() {
    return pauseOnLoadPF;
  }

  public void setPauseOnLoadPF(boolean pauseOnLoadPF) {
    this.pauseOnLoadPF = pauseOnLoadPF;
  }

  public int getAutoExitEmuSeconds() {
    return autoExitEmuSeconds;
  }

  public void setAutoExitEmuSeconds(int autoExitEmuSeconds) {
    this.autoExitEmuSeconds = autoExitEmuSeconds;
  }

  public int getIntroSkipSeconds() {
    return introSkipSeconds;
  }

  public void setIntroSkipSeconds(int introSkipSeconds) {
    this.introSkipSeconds = introSkipSeconds;
  }

  public boolean isAttractOnStart() {
    return attractOnStart;
  }

  public void setAttractOnStart(boolean attractOnStart) {
    this.attractOnStart = attractOnStart;
  }

  public boolean isMuteLaunchAudio() {
    return muteLaunchAudio;
  }

  public void setMuteLaunchAudio(boolean muteLaunchAudio) {
    this.muteLaunchAudio = muteLaunchAudio;
  }

  public int getWheelUpdateMS() {
    return wheelUpdateMS;
  }

  public void setWheelUpdateMS(int wheelUpdateMS) {
    this.wheelUpdateMS = wheelUpdateMS;
  }

  public int getFadeoutLoading() {
    return fadeoutLoading;
  }

  public void setFadeoutLoading(int fadeoutLoading) {
    this.fadeoutLoading = fadeoutLoading;
  }

  public int getLaunchTimeoutSecs() {
    return launchTimeoutSecs;
  }

  public void setLaunchTimeoutSecs(int launchTimeoutSecs) {
    this.launchTimeoutSecs = launchTimeoutSecs;
  }

  public boolean isJoyAxisMove() {
    return joyAxisMove;
  }

  public void setJoyAxisMove(boolean joyAxisMove) {
    this.joyAxisMove = joyAxisMove;
  }

  public boolean isVolumeChange() {
    return volumeChange;
  }

  public void setVolumeChange(boolean volumeChange) {
    this.volumeChange = volumeChange;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("DelayReturn=" + delayReturn);
    builder.append("\r\n");
    builder.append("ReturnNext=" + (returnNext ? "1" : "0"));
    builder.append("\r\n");
    builder.append("NoSysFavs=" + (noSysFavs ? "1" : "0"));
    builder.append("\r\n");
    builder.append("NoSysLists=" + (noSysLists ? "1" : "0"));
    builder.append("\r\n");
    builder.append("EmuExitCount=" + emuExitCount);
    builder.append("\r\n");
    builder.append("PlayOnlyMode=" + (playOnlyMode ? "1" : "0"));
    builder.append("\r\n");
    builder.append("WheelAniTimeMS=" + wheelAniTimeMS);
    builder.append("\r\n");
    builder.append("ShowInfoInGame=" + (showInfoInGame ? "1" : "0"));
    builder.append("\r\n");
    builder.append("PopUPHideAnykey=" + (popUPHideAnykey ? "1" : "0"));
    builder.append("\r\n");
    builder.append("RapidFireCount=" + rapidFireCount);
    builder.append("\r\n");
    builder.append("PauseOnLoad=" + (pauseOnLoad ? "1" : "0"));
    builder.append("\r\n");
    builder.append("PauseOnLoadPF=" + (pauseOnLoadPF ? "1" : "0"));
    builder.append("\r\n");
    builder.append("AutoExitEmuSeconds=" + autoExitEmuSeconds);
    builder.append("\r\n");
    builder.append("IntroSkipSeconds=" + introSkipSeconds);
    builder.append("\r\n");
    builder.append("AttractOnStart=" + (attractOnStart ? "1" : "0"));
    builder.append("\r\n");
    builder.append("muteLaunchAudio=" + (muteLaunchAudio ? "1" : "0"));
    builder.append("\r\n");
    builder.append("WheelUpdateMS=" + wheelUpdateMS);
    builder.append("\r\n");
    builder.append("FadeoutLoading=" + fadeoutLoading);
    builder.append("\r\n");
    builder.append("LaunchTimeoutSecs=" + launchTimeoutSecs);
    builder.append("\r\n");
    builder.append("JoyAxisMove=" + (joyAxisMove ? "1" : "0"));
    builder.append("\r\n");
    builder.append("VolumeChange=" + (volumeChange ? "1" : "0"));
    builder.append("\r\n");
    builder.append("useAltWheels=" + (useAltWheels ? "1" : "0"));

    return builder.toString();
  }

  public void setScriptData(String optionString) {
    if (optionString != null) {
      String[] split = optionString.split("\n");
      if (optionString.contains("\r")) {
        split = optionString.split("\r\n");
      }

      for (String s : split) {
        try {
          if (!StringUtils.isEmpty(s) && s.contains("=")) {
            String[] valueLine = s.split("=");
            String key = valueLine[0];
            int value = 0;
            try {
              value = Integer.parseInt(valueLine[1]);
            }
            catch (NumberFormatException e) {
              LOG.error("Failed to read script value line \"" + s + "\": " + e.getMessage());
              continue;
            }

            switch (key) {
              case "DelayReturn": {
                this.delayReturn = value;
                break;
              }
              case "ReturnNext": {
                this.returnNext = value == 1;
                break;
              }
              case "NoSysFavs": {
                this.noSysFavs = value == 1;
                break;
              }
              case "NoSysLists": {
                this.noSysLists = value == 1;
                break;
              }
              case "EmuExitCount": {
                this.emuExitCount = value;
                break;
              }
              case "PlayOnlyMode": {
                this.playOnlyMode = value == 1;
                break;
              }
              case "WheelAniTimeMS": {
                this.wheelAniTimeMS = value;
                break;
              }
              case "ShowInfoInGame": {
                this.showInfoInGame = value == 1;
                break;
              }
              case "PopUPHideAnykey": {
                this.popUPHideAnykey = value == 1;
                break;
              }
              case "RapidFireCount": {
                this.rapidFireCount = value;
                break;
              }
              case "PauseOnLoad": {
                this.pauseOnLoad = value == 1;
                break;
              }
              case "PauseOnLoadPF": {
                this.pauseOnLoadPF = value == 1;
                break;
              }
              case "AutoExitEmuSeconds": {
                this.autoExitEmuSeconds = value;
                break;
              }
              case "IntroSkipSeconds": {
                this.introSkipSeconds = value;
                break;
              }
              case "AttractOnStart": {
                this.attractOnStart = value == 1;
                break;
              }
              case "muteLaunchAudio": {
                this.muteLaunchAudio = value == 1;
                break;
              }
              case "WheelUpdateMS": {
                this.wheelUpdateMS = value;
                break;
              }
              case "FadeoutLoading": {
                this.fadeoutLoading = value;
                break;
              }
              case "LaunchTimeoutSecs": {
                this.launchTimeoutSecs = value;
                break;
              }
              case "JoyAxisMove": {
                this.joyAxisMove = value == 1;
                break;
              }
              case "VolumeChange": {
                this.volumeChange = value == 1;
                break;
              }
              case "useAltWheels": {
                this.useAltWheels = value == 1;
                break;
              }
              case "Watchdog": {
                this.watchDog = value == 1;
                break;
              }
            }
          }
        }
        catch (Exception e) {
          LOG.error("Failed to read script value line \"" + s + "\": " + e.getMessage());
        }
      }
    }
  }

  @Override
  public String getSettingsName() {
    return "PopperSettings";
  }
}
