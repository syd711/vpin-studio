package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UnzipChangeListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;

public class PupPackInstallerJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackInstallerJob.class);

  private final PupPacksService pupPacksService;
  private final File pupTmpArchive;
  private final File pupVideosFolder;
  private final String pupPackFolderInArchive;

  @NonNull
  private final String rom;

  private final boolean async;

  public PupPackInstallerJob(@NonNull PupPacksService pupPacksService, @NonNull File pupTmpArchive, @NonNull File pupVideosFolder, @NonNull String pupPackFolderInArchive, @NonNull String rom, boolean async) {
    this.pupPacksService = pupPacksService;
    this.pupTmpArchive = pupTmpArchive;
    this.pupVideosFolder = pupVideosFolder;
    this.pupPackFolderInArchive = pupPackFolderInArchive;
    this.rom = rom;
    this.async = async;
  }

  @Override
  public void execute(JobDescriptor result) {
    LOG.info("Starting PUP pack installation of '" + pupTmpArchive.getAbsolutePath() + "' to '" + pupVideosFolder.getAbsolutePath() + "', using archive root folder + '" + pupPackFolderInArchive + "' and ROM '" + rom + "'");
    File pupFolder = new File(pupVideosFolder, rom);
    PackageUtil.unpackTargetFolder(pupTmpArchive, pupFolder, pupPackFolderInArchive, Collections.emptyList(), new UnzipChangeListener() {
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

    if (async && pupTmpArchive.exists() & pupTmpArchive.delete()) {
      LOG.error("Delete temporary PUP pack archive {}", pupTmpArchive.getAbsolutePath());
    }

    pupPacksService.loadPupPack(rom);
  }
}
