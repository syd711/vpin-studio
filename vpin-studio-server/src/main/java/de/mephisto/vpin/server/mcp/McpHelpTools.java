package de.mephisto.vpin.server.mcp;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static de.mephisto.vpin.server.mcp.McpTableTools.prop;
import static de.mephisto.vpin.server.mcp.McpTableTools.schema;

/**
 * Answers "how do I ..." questions from the VPin Studio user documentation (GitHub wiki).
 */
@Service
public class McpHelpTools implements McpToolProvider {

  private static final int MAX_SECTION_CHARS = 4000;
  private static final int MAX_PAGE_CHARS = 40000;

  @Autowired
  private WikiHelpIndex wikiHelpIndex;

  @Override
  public List<McpTool> getTools() {
    List<McpTool> tools = new ArrayList<>();
    tools.add(McpTool.read("search_help", "Search the VPin Studio documentation",
        "Searches the VPin Studio user documentation (" + WikiHelpIndex.WIKI_URL + ") and returns the best matching "
            + "sections with their link. Use it for every question on how to use or set up VPin Studio, e.g. "
            + "\"iScored game room\", \"offline competition\", \"highscore cards\" or \"backup restore\". "
            + "Without a query the list of documentation pages is returned.",
        schema(Map.of(
            "query", prop("string", "Search terms, e.g. \"iScored game room setup\"."),
            "limit", prop("integer", "Maximum number of sections, default 5, maximum 10.")), List.of()),
        this::search));

    tools.add(McpTool.read("get_help_page", "Read a documentation page",
        "Returns a complete page of the VPin Studio user documentation, e.g. \"iScored\" or \"Competitions\". "
            + "Use it when the sections returned by search_help are not enough.",
        schema(Map.of("page", prop("string", "The page name or link as returned by search_help.")), List.of("page")),
        this::getPage));
    return tools;
  }

  private Object search(McpArguments args) {
    String query = StringUtils.trimToNull(args.getString("query"));
    if (query == null) {
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("documentation", WikiHelpIndex.WIKI_URL);
      result.put("pages", new ArrayList<>(wikiHelpIndex.getPages().keySet()));
      return result;
    }

    int limit = Math.max(1, Math.min(10, args.getInt("limit", 5)));
    List<Map<String, Object>> sections = new ArrayList<>();
    for (WikiHelpIndex.Section section : wikiHelpIndex.search(query, limit)) {
      Map<String, Object> s = new LinkedHashMap<>();
      s.put("page", section.page());
      s.put("section", section.title());
      s.put("url", section.url());
      s.put("text", StringUtils.abbreviate(section.text(), MAX_SECTION_CHARS));
      sections.add(s);
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("query", query);
    result.put("sections", sections);
    if (sections.isEmpty()) {
      result.put("message", "Nothing found. Call search_help without a query for the list of pages.");
    }
    return result;
  }

  private Object getPage(McpArguments args) {
    String name = StringUtils.trimToNull(args.getString("page"));
    if (name == null) {
      throw new McpToolException("Missing argument \"page\".");
    }
    String page = wikiHelpIndex.findPage(name);
    if (page == null) {
      throw new McpToolException("No documentation page \"" + name + "\". Available pages: "
          + String.join(", ", wikiHelpIndex.getPages().keySet()));
    }
    String markdown = WikiHelpIndex.clean(wikiHelpIndex.getPages().get(page));
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("page", page);
    result.put("url", WikiHelpIndex.WIKI_URL + "/" + page);
    result.put("markdown", StringUtils.abbreviate(markdown, MAX_PAGE_CHARS));
    return result;
  }
}
