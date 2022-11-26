package de.mephisto.vpin.server.competitions;

import java.util.Date;

public class CompetitionCleanupRunnableTask implements Runnable{

  public CompetitionCleanupRunnableTask(){
  }

  @Override
  public void run() {
    System.out.println(new Date()+" Runnable Task with "
        +" on thread "+Thread.currentThread().getName());
  }
}