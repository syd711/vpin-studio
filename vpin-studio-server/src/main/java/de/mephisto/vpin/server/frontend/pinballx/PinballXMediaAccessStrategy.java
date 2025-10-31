package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.DefaultMediaAccessStrategy;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.playlists.Playlist;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.util.StringUtils;

public class PinballXMediaAccessStrategy extends DefaultMediaAccessStrategy {

  private File pinballXFolder;

  private Map<VPinScreen, String[]> folders;
  private Set<VPinScreen> hasVideosOrImages;
  private Set<String> imageExtensions;

  public PinballXMediaAccessStrategy(File pinballXFolder) {
    this.pinballXFolder = pinballXFolder;

    imageExtensions = new HashSet<>();
      imageExtensions.add("png");
      imageExtensions.add("jpg");
      imageExtensions.add("jpeg");

    folders = new HashMap<>();
      folders.put(VPinScreen.Audio, new String[] { "Table Audio" });
      folders.put(VPinScreen.AudioLaunch, new String[] { "Launch Audio" });
      folders.put(VPinScreen.BackGlass, new String[] { "Backglass Videos", "Backglass Images" });
      folders.put(VPinScreen.DMD, new String[] { "DMD Videos", "DMD Images" });
      folders.put(VPinScreen.GameInfo, new String[] { "../Flyer Images/Front", "../Flyer Images/Back",
            "../Flyer Images/Inside1", "../Flyer Images/Inside2", "../Flyer Images/Inside3", 
            "../Flyer Images/Inside4", "../Flyer Images/Inside5", "../Flyer Images/Inside6" });
      folders.put(VPinScreen.GameHelp, new String[] { "../Instruction Cards" });
      folders.put(VPinScreen.Loading, new String[] { "../Loading Videos" });
      folders.put(VPinScreen.Menu, new String[] { "FullDMD Videos" });
      //folders.put(VPinScreen.Other2, null);
      folders.put(VPinScreen.PlayField, new String[] { "Table Videos", "Table Images" });
      folders.put(VPinScreen.Topper, new String[] { "Topper Videos", "Topper Images" });
      folders.put(VPinScreen.Wheel, new String[] { "Wheel Images" });

    // The asset types for which in pinballX could be stores as Images or Videos
    hasVideosOrImages = new HashSet<>();
      hasVideosOrImages.add(VPinScreen.Topper);
      hasVideosOrImages.add(VPinScreen.BackGlass);
      hasVideosOrImages.add(VPinScreen.DMD);
      hasVideosOrImages.add(VPinScreen.PlayField);
  }

  @Override
  public File getEmulatorMediaFolder(@NonNull GameEmulator emu, VPinScreen screen) {
    String mediaDirectory = emu.getMediaDirectory();
    return getMediaFolder(mediaDirectory, screen, null);
  }

  @Override
  public File getPlaylistMediaFolder(@NonNull Playlist playList, @NonNull VPinScreen screen, boolean create) {
    // not standard but why not...
    File mediaDir = new File(pinballXFolder, "Media/Playlists");
    return ensureDirExist(getMediaFolder(mediaDir.getAbsolutePath(), screen, null), create);
  }

  @Override
  public File getGameMediaFolder(@NonNull Game game, @NonNull VPinScreen screen, @Nullable String extension, boolean create) {
    String mediaDirectory = game.getEmulator().getMediaDirectory();
    return ensureDirExist(getMediaFolder(mediaDirectory, screen, extension), create);
  }

  private File getMediaFolder(String mediaDirectory,@NonNull  VPinScreen screen, @Nullable String extension) {
    String[] _folders = folders.get(screen);
    if (_folders == null) {
      return null;
    }
    if (extension == null || !hasVideosOrImages.contains(screen)) {
      return new File(mediaDirectory, _folders[0]);
    }

    // check from extension 
    return imageExtensions.contains(extension.toLowerCase())? 
      new File(mediaDirectory, _folders[1]) : 
      new File(mediaDirectory, _folders[0]);
  }

  //---------------------------
  @Override
  public List<File> getScreenMediaFiles(@NonNull Game game, @NonNull VPinScreen screen, @Nullable String mediaSearchTerm) {
    String mediaDirectory = game.getEmulator().getMediaDirectory();

    ArrayList<File> lists = new ArrayList<>();
    String[] _folders = folders.get(screen);
    if (_folders != null) {
      for (String folder : _folders) {
        File parent = new File(mediaDirectory, folder);
        File[] files = parent.listFiles((dir, name) -> StringUtils.startsWithIgnoreCase(name, game.getGameName()));
        if (files != null && files.length > 0) {
          for (File f : files) {
            lists.add(f);
          }
        }
      }
    }
    return lists;
  }

}
