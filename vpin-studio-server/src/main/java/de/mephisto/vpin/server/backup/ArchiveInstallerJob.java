package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.restclient.descriptors.ArchiveInstallDescriptor;
import de.mephisto.vpin.server.backup.types.TableInstallerAdapter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import edu.umd.cs.findbugs.annotations.NonNull;

public class ArchiveInstallerJob implements Job {
  private final TableInstallerAdapter tableInstallerAdapter;
  private final ArchiveDescriptor archiveDescriptor;
  private final PinUPConnector pinUPConnector;
  private final CardService cardService;
  private final ArchiveInstallDescriptor installDescriptor;

  public ArchiveInstallerJob(@NonNull TableInstallerAdapter tableInstallerAdapter,
                             @NonNull ArchiveDescriptor archiveDescriptor,
                             @NonNull PinUPConnector pinUPConnector,
                             @NonNull CardService cardService,
                             @NonNull ArchiveInstallDescriptor installDescriptor) {
    this.tableInstallerAdapter = tableInstallerAdapter;
    this.archiveDescriptor = archiveDescriptor;
    this.pinUPConnector = pinUPConnector;
    this.cardService = cardService;
    this.installDescriptor = installDescriptor;
  }

  @Override
  public double getProgress() {
    return tableInstallerAdapter.getProgress();
  }

  @Override
  public String getStatus() {
    return tableInstallerAdapter.getStatus();
  }

  @Override
  public boolean execute() {
    Game game = tableInstallerAdapter.installTable();
    if (game != null) {
      try {
        cardService.generateCard(game, false);
      } catch (Exception e) {
        //ignore
      }

      if (installDescriptor.getPlaylistId() != -1) {
        pinUPConnector.addToPlaylist(game.getId(), installDescriptor.getPlaylistId());
      }
    }
    return game != null;
  }
}
