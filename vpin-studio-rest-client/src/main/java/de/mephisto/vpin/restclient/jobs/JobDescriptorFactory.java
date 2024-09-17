package de.mephisto.vpin.restclient.jobs;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;

public class JobDescriptorFactory {

  public static JobDescriptor error(boolean success, String message) {
    if (success) {
      return new JobDescriptor();
    }
    return error(message);
  }

  public static JobDescriptor error(String message) {
    return error(message, -1);
  }

  public static JobDescriptor ok(int gameId) {
    JobDescriptor result = new JobDescriptor();
    result.setGameId(gameId);
    return result;
  }

  public static JobDescriptor error(String message, int gameId) {
    JobDescriptor result = new JobDescriptor();
    result.setError(message);
    result.setGameId(gameId);
    return result;
  }

  public static JobDescriptor empty() {
    return new JobDescriptor();
  }
}
