package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.vpx.TableScriptOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.mephisto.vpin.server.vpx.VpxScriptOptionsResource.SCRIPT_OPTIONS_API_SEGMENT;

/**
 * REST endpoints for reading and persisting VPX table script options.
 * <p>
 * GET  /api/v1/vpx/scriptoptions/{gameId}          → list of TableScriptOption
 * POST /api/v1/vpx/scriptoptions/{gameId}          → save updated values, returns true/false
 * POST /api/v1/vpx/scriptoptions/{gameId}/reset    → reset all options to defaults, returns true/false
 */
@RestController
@RequestMapping(SCRIPT_OPTIONS_API_SEGMENT)
public class VpxScriptOptionsResource {

  private static final Logger LOG = LoggerFactory.getLogger(VpxScriptOptionsResource.class);

  public static final String SCRIPT_OPTIONS_API_SEGMENT = "/api/v1/vpx/scriptoptions";

  @Autowired
  private VpxScriptOptionsService scriptOptionsService;

  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Parses the script for the given game and returns all discovered options,
   * each populated with its current value from the companion INI file.
   */
  @GetMapping("/{gameId}")
  public List<TableScriptOption> getOptions(@PathVariable int gameId) {
    LOG.info("GET script options for game {}", gameId);
    return scriptOptionsService.getOptions(gameId);
  }

  /**
   * Persists the {@code currentValue} of each supplied option to the companion
   * INI file.  Other INI settings (camera overrides, etc.) are preserved.
   *
   * @return {@code true} on success, {@code false} on failure.
   */
  @PostMapping("/{gameId}")
  public boolean saveOptions(@PathVariable int gameId,
                             @RequestBody List<TableScriptOption> options) {
    LOG.info("POST save {} script options for game {}", options.size(), gameId);
    return scriptOptionsService.saveOptions(gameId, options);
  }

  /**
   * Resets all options for the given game to their declared default values by
   * removing the [TableOptions] section from the companion INI file entries.
   *
   * @return {@code true} on success, {@code false} on failure.
   */
  @PostMapping("/{gameId}/reset")
  public boolean resetOptions(@PathVariable int gameId) {
    LOG.info("POST reset script options for game {}", gameId);
    // Load options with defaults and save them — this effectively writes
    // the default values into the INI, making the file state explicit.
    List<TableScriptOption> options = scriptOptionsService.getOptions(gameId);
    options.forEach(o -> o.setCurrentValue(o.getDefaultValue()));
    return scriptOptionsService.saveOptions(gameId, options);
  }
}
