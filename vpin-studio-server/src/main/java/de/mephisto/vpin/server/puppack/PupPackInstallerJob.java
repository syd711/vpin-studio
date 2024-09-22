package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.server.util.UnzipChangeListener;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;

public class PupPackInstallerJob implements Job {

  private final PupPacksService pupPacksService;
  private final File pupTmpArchive;
  private final File pupVideosFolder;
  private final String pupPackFolderInArchive;
  @NonNull
  private final String rom;

  public PupPackInstallerJob(@NonNull PupPacksService pupPacksService, @NonNull File pupTmpArchive, @NonNull File pupVideosFolder, @NonNull String pupPackFolderInArchive, @NonNull String rom) {
    this.pupPacksService = pupPacksService;
    this.pupTmpArchive = pupTmpArchive;
    this.pupVideosFolder = pupVideosFolder;
    this.pupPackFolderInArchive = pupPackFolderInArchive;
    this.rom = rom;
  }

  @Override
  public void execute(JobDescriptor result) {
    PupPackUtil.unpack(pupTmpArchive, pupVideosFolder, pupPackFolderInArchive, rom, new UnzipChangeListener() {
      @Override
      public boolean unzipping(String name, int index, int total) {
        double progress = (double) (100 * index / total) / 100;
        result.setProgress(progress);
        result.setStatus("Unpacking " + index + " of " + total);

        boolean cancelled = result.isCancelled();
        return !cancelled;
      }

      @Override
      public void onError(String error) {
        result.setError(error);
      }
    });

    if (!result.isCancelled()) {
      result.setProgress(1);
    }
    pupPacksService.loadPupPack(rom);
  }
}
