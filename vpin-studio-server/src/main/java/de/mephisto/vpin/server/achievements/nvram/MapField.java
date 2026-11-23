package de.mephisto.vpin.server.achievements.nvram;

import tools.jackson.databind.JsonNode;
import org.jspecify.annotations.NonNull;

/**
 * A single field of a NVRAM map as the Achievement Watcher sees it: the label used in achievement titles,
 * the top level section it was found in (used for the "priority fields" of the rule generation) and the
 * raw JSON node used to decode the value.
 */
public record MapField(@NonNull String label, @NonNull String section, @NonNull JsonNode node) {
}
