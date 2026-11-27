package de.mephisto.vpin.server.mcp;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Searchable copy of the VPin Studio user documentation, the GitHub wiki. The pages are loaded from GitHub on first
 * use (the page list comes from the wiki sidebar) and kept for a day.
 */
@Component
public class WikiHelpIndex {
  private final static Logger LOG = LoggerFactory.getLogger(WikiHelpIndex.class);

  public static final String WIKI_URL = "https://github.com/syd711/vpin-studio/wiki";
  private static final String RAW_URL = "https://raw.githubusercontent.com/wiki/syd711/vpin-studio/";
  private static final Duration MAX_AGE = Duration.ofHours(24);

  private static final Pattern PAGE_LINK = Pattern.compile("https://github\\.com/syd711/vpin-studio/wiki(?:/([^)#\\s]+))?");
  private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*)$");
  private static final Pattern IMAGE = Pattern.compile("<img[^>]*>|!\\[[^]]*]\\([^)]*\\)", Pattern.CASE_INSENSITIVE);
  private static final Pattern HTML_TAG = Pattern.compile("</?[a-zA-Z][^>]*>");
  private static final Set<String> STOP_WORDS = Set.of("the", "and", "for", "how", "can", "what", "with", "does", "do", "is", "are",
      "to", "of", "in", "on", "a", "an", "my", "i", "it", "be", "set", "up", "use", "vpin", "studio");

  private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NORMAL).build();

  private volatile Map<String, String> pages;
  private volatile long loadedAt;

  public record Section(String page, String title, String anchor, String text) {
    public String url() {
      return WIKI_URL + "/" + page + (anchor != null ? "#" + anchor : "");
    }
  }

  /**
   * @return page name -> markdown, sorted by page name
   */
  public synchronized Map<String, String> getPages() {
    if (pages == null || System.currentTimeMillis() - loadedAt > MAX_AGE.toMillis()) {
      String sidebar = fetch(RAW_URL + "_Sidebar.md");
      if (sidebar == null) {
        if (pages != null) {
          return pages; //keep the old copy when GitHub is not reachable
        }
        throw new McpToolException("The documentation could not be loaded from GitHub. It can be read at " + WIKI_URL);
      }

      List<String> names = parsePageNames(sidebar);
      Map<String, CompletableFuture<String>> requests = new LinkedHashMap<>();
      for (String name : names) {
        requests.put(name, CompletableFuture.supplyAsync(() -> fetch(RAW_URL + encode(name) + ".md")));
      }
      Map<String, String> loaded = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      requests.forEach((name, request) -> {
        String markdown = request.join();
        if (markdown != null) {
          loaded.put(name, markdown);
        }
      });
      LOG.info("Loaded {} of {} wiki pages for the MCP help.", loaded.size(), names.size());
      pages = loaded;
      loadedAt = System.currentTimeMillis();
    }
    return pages;
  }

  public List<Section> search(String query, int limit) {
    List<String> terms = terms(query);
    if (terms.isEmpty()) {
      return List.of();
    }

    record Hit(Section section, int score) {
    }
    List<Hit> hits = new ArrayList<>();
    getPages().forEach((page, markdown) -> {
      String pageLower = page.toLowerCase(Locale.ROOT).replace('-', ' ');
      for (Section section : sections(page, markdown)) {
        String title = section.title().toLowerCase(Locale.ROOT);
        String text = section.text().toLowerCase(Locale.ROOT);
        int score = 0;
        int matchedTerms = 0;
        for (String term : terms) {
          int termScore = (title.contains(term) ? 6 : 0) + (pageLower.contains(term) ? 3 : 0) + Math.min(5, StringUtils.countMatches(text, term));
          if (termScore > 0) {
            matchedTerms++;
            score += termScore;
          }
        }
        if (matchedTerms > 0) {
          //sections matching all terms first
          hits.add(new Hit(section, score + matchedTerms * 10));
        }
      }
    });
    return hits.stream().sorted(Comparator.comparingInt(Hit::score).reversed()).limit(limit).map(Hit::section).toList();
  }

  public String findPage(String name) {
    Map<String, String> all = getPages();
    String normalized = normalizePageName(name);
    for (String page : all.keySet()) {
      if (normalizePageName(page).equals(normalized)) {
        return page;
      }
    }
    return null;
  }

  //-------------------------------- parsing -----------------------------------------------------------

  static List<String> parsePageNames(String sidebar) {
    Set<String> names = new LinkedHashSet<>();
    names.add("Home");
    Matcher matcher = PAGE_LINK.matcher(sidebar);
    while (matcher.find()) {
      String name = matcher.group(1);
      if (name != null && !name.isBlank()) {
        names.add(URLDecoder.decode(name, StandardCharsets.UTF_8));
      }
    }
    return new ArrayList<>(names);
  }

  /**
   * Splits a page at its headings. The text of a section is cleaned from images and HTML tags.
   */
  static List<Section> sections(String page, String markdown) {
    List<Section> result = new ArrayList<>();
    String title = page.replace('-', ' ');
    String anchor = null;
    StringBuilder text = new StringBuilder();
    for (String line : markdown.split("\\r?\\n")) {
      Matcher heading = HEADING.matcher(line);
      if (heading.matches()) {
        addSection(result, page, title, anchor, text);
        title = clean(heading.group(2)).trim();
        anchor = anchor(title);
        text.setLength(0);
      }
      else {
        text.append(clean(line)).append('\n');
      }
    }
    addSection(result, page, title, anchor, text);
    return result;
  }

  private static void addSection(List<Section> result, String page, String title, String anchor, StringBuilder text) {
    String content = text.toString().replaceAll("\\n{3,}", "\n\n").trim();
    if (!content.isEmpty()) {
      result.add(new Section(page, title, anchor, content));
    }
  }

  static String clean(String markdown) {
    String text = IMAGE.matcher(markdown).replaceAll("[image]");
    return HTML_TAG.matcher(text).replaceAll("");
  }

  /**
   * GitHub heading anchors: lower case, punctuation removed, spaces replaced by dashes.
   */
  static String anchor(String title) {
    String a = title.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N} _-]", "").trim();
    return a.replace(' ', '-');
  }

  static List<String> terms(String query) {
    if (query == null) {
      return List.of();
    }
    return Arrays.stream(query.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}.]+"))
        .filter(t -> t.length() >= 2 && !STOP_WORDS.contains(t))
        .distinct().toList();
  }

  private static String normalizePageName(String name) {
    return URLDecoder.decode(StringUtils.defaultString(name), StandardCharsets.UTF_8)
        .replaceFirst("^.*/wiki/?", "").replaceFirst("#.*$", "")
        .toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
  }

  private static String encode(String name) {
    return URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
  }

  /**
   * @return the body of the URL, null if it could not be loaded
   */
  String fetch(String url) {
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15)).GET().build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() == 200) {
        return response.body();
      }
      LOG.warn("Loading {} failed: HTTP {}", url, response.statusCode());
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    catch (Exception e) {
      LOG.warn("Loading {} failed: {}", url, e.getMessage());
    }
    return null;
  }
}
