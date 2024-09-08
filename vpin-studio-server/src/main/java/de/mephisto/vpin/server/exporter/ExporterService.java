package de.mephisto.vpin.server.exporter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExporterService {
  private final static String PARAM_DELIMITER = "delimiter";
  private final static String PARAM_ESCAPE = "escape";
  private final static String PARAM_QUOTE = "quote";

  protected @NotNull CSVPrinter createPrinter(Map<String, String> customQuery, List<String> headers, StringBuilder builder) throws IOException {
    String delimiter = ";";
    if (customQuery.containsKey(PARAM_DELIMITER)) {
      delimiter = customQuery.get(PARAM_DELIMITER);
    }
    char quote = '\"';
    if (customQuery.containsKey(PARAM_QUOTE)) {
      quote = customQuery.get(PARAM_QUOTE).charAt(0);
    }

    char escape = '\\';
    if (customQuery.containsKey(PARAM_ESCAPE)) {
      escape = customQuery.get(PARAM_ESCAPE).charAt(0);
    }

    return CSVFormat.DEFAULT.builder()
        .setHeader(headers.toArray(new String[0]))
        .setDelimiter(delimiter)
        .setTrailingDelimiter(true)
        .setEscape(escape)
        .setQuote(quote)
        .build().print(builder);
  }
}
