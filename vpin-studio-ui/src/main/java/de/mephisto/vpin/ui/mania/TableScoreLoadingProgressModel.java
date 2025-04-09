package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.TableScore;
import de.mephisto.vpin.ui.mania.widgets.ManiaWidgetPlayerStatsController;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TableScoreLoadingProgressModel extends ProgressModel<Account> {
  private final static Logger LOG = LoggerFactory.getLogger(TableScoreLoadingProgressModel.class);

  private final Account account;
  private final Iterator<Account> iterator;

  public TableScoreLoadingProgressModel(Account account) {
    super("Loading Player Data");
    this.account = account;
    this.iterator = Arrays.asList(account).iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public Account getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(Account acc) {
    return "";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, Account account) {
    try {
      List<TableScore> highscoresByAccount = new ArrayList<>(maniaClient.getHighscoreClient().getHighscoresByAccount(account.getUuid()));
      List<ManiaWidgetPlayerStatsController.TableScoreModel> models = new ArrayList<>();
      for (TableScore tableScore : highscoresByAccount) {
        models.add(new ManiaWidgetPlayerStatsController.TableScoreModel(tableScore));
      }

      progressResultModel.getResults().add(models);
    }
    catch (Exception e) {
      LOG.error("Error during loading account scores: " + e.getMessage(), e);
    }
  }
}
