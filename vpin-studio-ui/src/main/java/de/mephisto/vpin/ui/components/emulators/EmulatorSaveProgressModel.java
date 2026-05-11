package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class EmulatorSaveProgressModel extends ProgressModel<GameEmulatorRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorSaveProgressModel.class);

  private final List<GameEmulatorRepresentation> emulators;
  private final EmulatorsController emulatorsController;
  private final Iterator<GameEmulatorRepresentation> iterator;

  public EmulatorSaveProgressModel(GameEmulatorRepresentation emulator, EmulatorsController emulatorsController) {
    super("Saving \"" + emulator.getName()+  "\"");
    this.emulators = Arrays.asList(emulator);
    this.emulatorsController = emulatorsController;
    this.iterator = emulators.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public GameEmulatorRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return this.emulators.size() == 1;
  }

  @Override
  public String nextToString(GameEmulatorRepresentation c) {
    return "Saving emulator and validating game list";
  }

  @Override
  public int getMax() {
    return emulators.size();
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);
    Platform.runLater(() -> {
      emulatorsController.onReload();
    });
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameEmulatorRepresentation next) {
    try {
      client.getEmulatorService().saveGameEmulator(next);
      client.getGameService().clearCache(next.getId());
    } catch (Exception e) {
      LOG.error("Failed to save emulator: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
