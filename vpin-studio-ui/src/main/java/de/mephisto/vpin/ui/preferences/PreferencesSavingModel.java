package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class PreferencesSavingModel extends ProgressModel<JsonSettings> {
  private final static Logger LOG = LoggerFactory.getLogger(PreferencesSavingModel.class);

  private final Iterator<JsonSettings> iterator;
  private final List<JsonSettings> settings;

  public PreferencesSavingModel(String title, JsonSettings settings) {
    this(title, Arrays.asList(settings));
  }

  public PreferencesSavingModel(String title, List<JsonSettings> settings) {
    super(title);
    this.settings = settings;
    this.iterator = settings.iterator();
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
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public JsonSettings getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return settings.size() == 1;
  }

  @Override
  public String nextToString(JsonSettings settings) {
    return null;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, JsonSettings settings) {
    try {
      client.getPreferenceService().setJsonPreference(settings);
    } catch (Exception e) {
      progressResultModel.getResults().add("Error saving settings: " + e.getMessage());
      LOG.error("Error saving settings: " + e.getMessage(), e);
    }
  }
}
