package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class PupPackInstallerJob implements Job {

  private final PupPacksService pupPacksService;
  private final File pupArchive;
  private final File pupVideosFolder;
  private final Game game;

  public PupPackInstallerJob(@NonNull PupPacksService pupPacksService, @NonNull File pupArchive, @NonNull File pupVideosFolder, @NonNull Game game) {
    this.pupPacksService = pupPacksService;
    this.pupArchive = pupArchive;
    this.pupVideosFolder = pupVideosFolder;
    this.game = game;
  }

  @Override
  public JobExecutionResult execute() {
    JobExecutionResult unzip = PupPackUtil.unpack(pupArchive, pupVideosFolder, game.getRom(), game.getTableName());
    if (!pupArchive.delete() && unzip.getError() == null) {
      return JobExecutionResultFactory.error("Failed to delete temporary file.");
    }

    if (StringUtils.isEmpty(unzip.getError())) {
      File target = new File(pupVideosFolder, game.getRom());
      if (!target.exists() && !StringUtils.isEmpty(game.getTableName())) {
        target = new File(pupVideosFolder, game.getTableName());
      }

      if(!target.exists()) {
        return JobExecutionResultFactory.error("Extracting PUP pack failed. Folder not found: " + target.getAbsolutePath());
      }

      pupPacksService.loadPupPack(target);
    }
    return unzip;
  }

  @Override
  public double getProgress() {
    return 0;
  }

  @Override
  public String getStatus() {
    return "Unzipping PUP pack for \"" + game.getGameDisplayName() + "\"";
  }
}
