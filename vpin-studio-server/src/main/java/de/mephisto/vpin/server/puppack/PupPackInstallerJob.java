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
  private final File pupTmpArchive;
  private final File pupVideosFolder;

  public PupPackInstallerJob(@NonNull PupPacksService pupPacksService, @NonNull File pupTmpArchive, @NonNull File pupVideosFolder) {
    this.pupPacksService = pupPacksService;
    this.pupTmpArchive = pupTmpArchive;
    this.pupVideosFolder = pupVideosFolder;
  }

  @Override
  public JobExecutionResult execute() {
    JobExecutionResult unzip = PupPackUtil.unpack(pupTmpArchive, pupVideosFolder);
    if (!pupTmpArchive.delete()) {
      return JobExecutionResultFactory.error("Failed to delete temporary file.");
    }
    pupPacksService.clearCache();
    return JobExecutionResultFactory.empty();
  }

  @Override
  public double getProgress() {
    return 0;
  }

  @Override
  public String getStatus() {
    return "Unzipping PUP pack";
  }
}
