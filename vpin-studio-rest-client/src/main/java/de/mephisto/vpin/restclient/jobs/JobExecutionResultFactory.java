package de.mephisto.vpin.restclient.jobs;

public class JobExecutionResultFactory {

  public static JobExecutionResult create(boolean success, String message) {
    if(success) {
      return new JobExecutionResult();
    }
    return create(message);
  }

  public static JobExecutionResult create(String message) {
    return create(message, -1);
  }

  public static JobExecutionResult create(String message, int gameId) {
    JobExecutionResult result = new JobExecutionResult();
    result.setError(message);
    result.setGameId(gameId);
    return result;
  }

  public static JobExecutionResult empty() {
    return new JobExecutionResult();
  }
}
