package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.TableScore;
import de.mephisto.vpin.connectors.mania.model.TableScoreDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.mania.widgets.ManiaWidgetPlayerStatsController;
import de.mephisto.vpin.ui.util.DismissalUtil;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TableScoreLoadingProgressModel extends ProgressModel<TableScore> {
  private final static Logger LOG = LoggerFactory.getLogger(TableScoreLoadingProgressModel.class);

  private final Account account;
  private List<TableScore> tableScores;

  private final Iterator<TableScore> tableScoresIterator;

  public TableScoreLoadingProgressModel(Account account, List<TableScore> tableScores) {
    super("Loading \"" + account.getDisplayName() + "\"");
    this.account = account;
    this.tableScores = tableScores;
    this.tableScoresIterator = tableScores.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return tableScores.size();
  }

  @Override
  public boolean hasNext() {
    return this.tableScoresIterator.hasNext();
  }

  @Override
  public TableScore getNext() {
    return tableScoresIterator.next();
  }

  @Override
  public String nextToString(TableScore score) {
    return score.getTableName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, TableScore score) {
    try {
      List<TableScoreDetails> highscoresByTable = maniaClient.getHighscoreClient().getHighscoresByTable(score.getVpsTableId());
      Collections.sort(highscoresByTable, (o1, o2) -> Long.compare(o2.getScore(), o1.getScore()));
      progressResultModel.getResults().add(new ManiaWidgetPlayerStatsController.TableScoreModel(score, account, highscoresByTable));
    }
    catch (Exception e) {
      LOG.error("Error during loading account scores: " + e.getMessage(), e);
    }
  }
}
