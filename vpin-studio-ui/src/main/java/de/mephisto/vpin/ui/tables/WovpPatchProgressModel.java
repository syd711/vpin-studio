package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.wovp.patcher.WovpPatchStatus;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.wovp.WovpPatchResult;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * Applies or removes the WoVP telemetry patch for the selected tables.
 */
public class WovpPatchProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final List<GameRepresentation> games;
  private final Iterator<GameRepresentation> gameIterator;
  private final boolean patch;
  private final boolean backup;

  private final List<WovpPatchResult> results = new ArrayList<>();

  /**
   * Asks for confirmation, runs the patch and shows a summary.
   *
   * @param patch true to add the telemetry, false to remove it
   */
  public static void run(List<GameRepresentation> games, boolean patch) {
    if (games.isEmpty()) {
      return;
    }

    boolean backup;
    if (patch) {
      ConfirmationResult confirmation = WidgetFactory.showConfirmationWithCheckbox(Studio.stage,
          Messages.get("wovp.patch.confirm", games.size()), Messages.get("wovp.patch.confirm.ok"),
          Messages.get("wovp.patch.confirm.help1"), Messages.get("wovp.patch.confirm.help2"),
          Messages.get("wovp.patch.confirm.backup"), true);
      if (!confirmation.isOkClicked()) {
        return;
      }
      backup = confirmation.isChecked();
    }
    else {
      var confirmation = WidgetFactory.showConfirmation(Studio.stage,
          Messages.get("wovp.unpatch.confirm", games.size()), Messages.get("wovp.unpatch.confirm.help1"),
          Messages.get("wovp.patch.confirm.help2"), Messages.get("wovp.unpatch.confirm.ok"));
      if (confirmation.isEmpty() || !confirmation.get().equals(ButtonType.OK)) {
        return;
      }
      backup = false;
    }

    WovpPatchProgressModel model = new WovpPatchProgressModel(games, patch, backup);
    ProgressDialog.createProgressDialog(model);
    model.showSummary();
  }

  private WovpPatchProgressModel(List<GameRepresentation> games, boolean patch, boolean backup) {
    super(Messages.get(patch ? "wovp.patch.progress" : "wovp.unpatch.progress"));
    this.games = games;
    this.gameIterator = games.iterator();
    this.patch = patch;
    this.backup = backup;
  }

  @Override
  public boolean isIndeterminate() {
    return games.size() == 1;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return games.size();
  }

  @Override
  public boolean hasNext() {
    return gameIterator.hasNext();
  }

  @Override
  public GameRepresentation getNext() {
    return gameIterator.next();
  }

  @Override
  public String nextToString(GameRepresentation game) {
    return game.getGameDisplayName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    WovpPatchResult result;
    try {
      result = patch ? client.getWovpService().patch(game.getId(), backup) : client.getWovpService().unpatch(game.getId(), backup);
    }
    catch (Exception e) {
      LOG.error("WoVP patch failed for {}: {}", game.getGameDisplayName(), e.getMessage(), e);
      result = new WovpPatchResult(game.getId(), WovpPatchStatus.FAILED);
      result.setMessage(e.getMessage());
    }

    synchronized (results) {
      results.add(result);
    }
    if (result.getStatus() == WovpPatchStatus.FAILED) {
      progressResultModel.addError();
    }
    else {
      progressResultModel.addProcessed(result);
    }
    if (result.getStatus() == WovpPatchStatus.PATCHED || result.getStatus() == WovpPatchStatus.UNPATCHED) {
      EventManager.getInstance().notifyTableChange(game.getId(), game.getRom());
    }
  }

  private void showSummary() {
    Map<WovpPatchStatus, Integer> counts = new EnumMap<>(WovpPatchStatus.class);
    List<String> errors = new ArrayList<>();
    synchronized (results) {
      for (WovpPatchResult result : results) {
        counts.merge(result.getStatus(), 1, Integer::sum);
        if (result.getStatus() == WovpPatchStatus.FAILED) {
          String name = games.stream().filter(g -> g.getId() == result.getGameId()).map(GameRepresentation::getGameDisplayName).findFirst().orElse(String.valueOf(result.getGameId()));
          errors.add(Messages.get("wovp.patch.failed", name, result.getMessage()));
        }
      }
    }
    if (results.isEmpty()) {
      return;
    }

    List<String> lines = new ArrayList<>();
    for (Map.Entry<WovpPatchStatus, Integer> entry : counts.entrySet()) {
      lines.add(Messages.get("wovp.patch.result." + entry.getKey().name().toLowerCase(), entry.getValue()));
    }
    String summary = String.join(", ", lines);

    if (errors.isEmpty()) {
      WidgetFactory.showInformation(Studio.stage, Messages.get("wovp.patch.summary"), summary);
    }
    else {
      WidgetFactory.showAlert(Studio.stage, Messages.get("wovp.patch.summary"), summary, String.join("\n", errors));
    }
  }
}
