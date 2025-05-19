package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompetitionLifecycleService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionLifecycleService.class);

  private final List<CompetitionChangeListener> listeners = new ArrayList<>();


  public void notifyCompetitionCreation(@NonNull Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionCreated(c);
    }
  }

  public void notifyCompetitionStarted(@NonNull Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionStarted(c);
    }
  }

  public void notifyCompetitionChanged(@NonNull Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionChanged(c);
    }
  }

  public void notifyCompetitionDeleted(@NonNull Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionDeleted(c);
    }
  }

  public void notifyCompetitionFinished(Competition finishedCompetition, Player player, ScoreSummary competitionScore) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionFinished(finishedCompetition, player, competitionScore);
    }
  }

  public void addCompetitionChangeListener(CompetitionChangeListener c) {
    this.listeners.add(c);
  }

  @Override
  public void afterPropertiesSet() throws Exception {

  }
}
