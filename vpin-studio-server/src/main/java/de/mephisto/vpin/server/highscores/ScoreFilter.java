package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ScoreFilter implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(ScoreFilter.class);

  @Autowired
  private PreferencesService preferencesService;

  private boolean highscoreFilterEnabled;
  private List<String> allowList;

  public boolean isScoreFiltered(@NonNull Score score) {
    if (StringUtils.isEmpty(score.getPlayerInitials())) {
      LOG.info("Filtered highscore update \"" + score + "\": player initials are empty");
      return true;
    }
    if (score.getPlayerInitials().equalsIgnoreCase("???")) {
      LOG.info("Filtered highscore update \"" + score + "\": player initials are ???");
      return true;
    }

    if (highscoreFilterEnabled) {
      if (!allowList.contains(score.getPlayerInitials())) {
        LOG.info("Filtered highscore update \"" + score + "\": player initials '" + score.getPlayerInitials() + "' are not on the allow list");
        return true;
      }
    }
    return false;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);

    refreshScoreFilterSettings();
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (!StringUtils.isEmpty(propertyName)) {
      if (propertyName.equals(PreferenceNames.HIGHSCORE_ALLOW_LIST) || propertyName.equals(PreferenceNames.HIGHSCORE_FILTER_ENABLED)) {
        refreshScoreFilterSettings();
      }
    }
  }

  private void refreshScoreFilterSettings() {
    highscoreFilterEnabled = (boolean) preferencesService.getPreferenceValue(PreferenceNames.HIGHSCORE_FILTER_ENABLED, false);
    allowList = Arrays.asList(((String) preferencesService.getPreferenceValue(PreferenceNames.HIGHSCORE_ALLOW_LIST, "")).split(","));
    LOG.info("Loaded allow list settings: " + allowList.size() + " entries found: " + String.join(", ", allowList));
  }
}
