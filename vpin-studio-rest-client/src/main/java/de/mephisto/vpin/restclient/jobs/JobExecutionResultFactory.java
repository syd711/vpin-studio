package de.mephisto.vpin.restclient.jobs;

public class JobExecutionResultFactory {

  public static JobExecutionResult error(boolean success, String message) {
    if (success) {
      return new JobExecutionResult();
    }
    return error(message);
  }

  public static JobExecutionResult error(String message) {
    return error(message, -1);
  }

  public static JobExecutionResult ok(String ok, int gameId) {
    JobExecutionResult result = new JobExecutionResult();
    result.setMessage(ok);
    result.setGameId(gameId);
    return result;
  }

  public static JobExecutionResult error(String message, int gameId) {
    JobExecutionResult result = new JobExecutionResult();
    result.setError(message);
    result.setGameId(gameId);
    return result;
  }

  public static JobExecutionResult error(String error, String ok, int gameId) {
    JobExecutionResult result = new JobExecutionResult();
    result.setError(error);
    result.setMessage(ok);
    result.setGameId(gameId);
    return result;
  }

  public static JobExecutionResult empty() {
    return new JobExecutionResult();
  }
}
