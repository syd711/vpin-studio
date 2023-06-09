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
  private final File out;
  private final File pupPackFolder;
  private final Game game;

  public PupPackInstallerJob(@NonNull PupPacksService pupPacksService, @NonNull File out, @NonNull File pupPackFolder, @NonNull Game game) {
    this.pupPacksService = pupPacksService;
    this.out = out;
    this.pupPackFolder = pupPackFolder;
    this.game = game;
  }

  @Override
  public JobExecutionResult execute() {
    JobExecutionResult unzip = PupPackUtil.unzip(out, pupPackFolder, game.getRom());
    if (!out.delete() && unzip.getError() == null) {
      return JobExecutionResultFactory.create("Failed to delete temporary file.");
    }

    if (StringUtils.isEmpty(unzip.getError())) {
      pupPacksService.addPupPack(pupPackFolder);
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
