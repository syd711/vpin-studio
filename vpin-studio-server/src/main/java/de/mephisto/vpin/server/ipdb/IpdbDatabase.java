package de.mephisto.vpin.server.ipdb;

 

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSubmitInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;


public class IpdbDatabase {

  private final static Logger LOG = LoggerFactory.getLogger(IpdbDatabase.class);

  public final static String URL = "https://www.ipdb.org/lists.cgi";

  public final static String BASE_URL = "https://www.ipdb.org/";

  private final IpdbSettings settings;

  private List<IpdbTable> tables = new ArrayList<>();

  //----------------- 

  public IpdbDatabase(IpdbSettings settings) {
    this.settings = settings;

    File ipdbFile = this.getIpdbFile();
    if (!ipdbFile.getParentFile().exists()) {
      ipdbFile.getParentFile().mkdirs();
    }
  }

  public IpdbTable getTableById(String id) {
    if (this.tables != null) {
      return this.tables.stream().filter(t -> t.getId() != null && t.getId().equals(id)).findFirst().orElse(null);
    }
    return null;
  }

  public List<IpdbTable> getTables() {
    return tables;
  }

  //----------------- FINDERS


  public List<IpdbTable> find(String searchTerm) {
    String term = searchTerm;
    term = term.replaceAll("_", " ");
    term = term.replaceAll("'", " ");
    term = term.replaceAll("-", " ");
    term = term.replaceAll("\\.", " ");
    term = term.replaceAll("The ", "");
    term = term.replaceAll(", The", "");
    if (term.contains("(")) {
      term = term.substring(0, term.indexOf("("));
    }
    term = term.toLowerCase().trim();

    List<IpdbTable> results = findInternal(term);

    while (results.isEmpty()) {
      if (term.contains(" ")) {
        term = term.substring(0, term.lastIndexOf(" "));
      }
      else {
        break;
      }
      results = findInternal(term);
    }

    return results;
  }

  private List<IpdbTable> findInternal(String term) {
    List<IpdbTable> results = new ArrayList<>();
    for (IpdbTable table : this.tables) {
      String name = table.getName().toLowerCase();
      name = name.replaceAll("-", " ");
      name = name.replaceAll("'", " ");
      name = name.replaceAll("'", " ");
      name = name.replaceAll("&", "and");
      if (!name.contains(term)) {
        continue;
      }
      results.add(table);
    }
    return results;
  }

  //----------------- LOADERS

  private File getIpdbFile() {
    File folder = new File("./resources");
    if (!folder.exists()) {
      folder = new File("../resources");
    }
    return new File(folder, "ipdb.html");
  }

  public void update() {
    try (final WebClient webClient = new WebClient()) {
      webClient.setCssErrorHandler(new SilentCssErrorHandler());
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setJavaScriptEnabled(false);

      String listUrl = login(webClient);
      if (listUrl == null) {
        LOG.error("IPDB Database cannot be updated, update ignored! :");
        LOG.error("Cannot login or cannot find link to gamelist, please report the error !");
        return;
      }

      // now load database within same WebClient
      updateDatabase(webClient, listUrl);
    }
    catch (IOException e) {
      LOG.error("Login failed: {}", e.getMessage(), e);
    }
  }

  private void updateDatabase(WebClient webClient, String listUrl) throws IOException {
    File vpsDbFile = getIpdbFile();

    LOG.info("Downloading tables from " + listUrl);

    File tmp = new File(vpsDbFile.getParentFile(), vpsDbFile.getName() + ".tmp");
    if (tmp.exists() && !tmp.delete()) {
      LOG.error("Failed to delete existing tmp file vpsdb.json.tmp");
    }

    try (FileOutputStream fos = new FileOutputStream(tmp)) {
      WebRequest req = new WebRequest(new URL(BASE_URL + listUrl));
      WebResponse res = webClient.getWebConnection().getResponse(req);
      IOUtils.copy(res.getContentAsStream(), fos);
    }

    long oldSize = 0;
    if (vpsDbFile.exists()) {
      oldSize = vpsDbFile.length();
    }

    if (vpsDbFile.exists() && !vpsDbFile.delete()) {
      LOG.error("Failed to delete " + vpsDbFile.getName());
    }
    else if (!tmp.renameTo(vpsDbFile)) {
      LOG.error("Failed to rename "+ tmp.getName() + " to " + vpsDbFile.getName());
    }
    else {
      LOG.info("Written " + vpsDbFile.getAbsolutePath() + ", (" + oldSize + " vs " + vpsDbFile.length() + " bytes)");
    }
  }

  public boolean reload() {
    File vpsDbFile = getIpdbFile();
    if (vpsDbFile.exists()) {
      try (InputStream fin = new FileInputStream(vpsDbFile)) {
        loadTables(fin);
        return true;
      }
      catch (IOException e) {
        LOG.error("Failed to reload VPS file: " + e.getMessage(), e);
      }
    }
    return false;
  }
  
  protected String login(WebClient webClient) throws IOException {
    // don't even try to authenticate if settings are not set
    if (StringUtils.isBlank(settings.getLogin())) {
      return "Login cannot be empty";
    }

    final HtmlPage loginPage = webClient.getPage(URL);

    HtmlForm loginForm = loginPage.getFormByName("login");
    loginForm.getInputByName("email").setValue(settings.getLogin());
    loginForm.getInputByName("password").setValue(settings.getPassword());
    HtmlSubmitInput button = loginForm.getInputByName("submit");
    final HtmlPage homePage = button.click();

    // check that authentication happens successfully
    String title = homePage.getTitleText();
    if (StringUtils.equalsIgnoreCase(title, "Pinball DataBase Lists")) {
      DomElement firstlink = homePage.getElementsByTagName("a").get(0);
      if (firstlink != null && StringUtils.equalsIgnoreCase(firstlink.getTextContent(), "Alphabetical Game Listing")) {
        return firstlink.getAttribute("href");
      }
    }
    return null;
  }


  public void loadTables(InputStream in) {
    try {
      String html = IOUtils.toString(in, Charset.defaultCharset());
      this.tables = parseTables(html);
      LOG.info(this.tables.size() + " Tables loaded from database");
    }
    catch (Exception e) {
      LOG.error("Failed to load IPDB database: " + e.getMessage(), e);
      this.tables = Collections.emptyList();
    }
  }


  private List<IpdbTable> parseTables(String html) {
    List<IpdbTable> tables = new ArrayList<>();
    int pos = -1;
    while ((pos = html.indexOf("<tr>", pos)) >= 0) {
      int end = html.indexOf("</tr>", pos);
      String table = html.substring(pos + 4, end);
      IpdbTable t = parseTable(table);
      if (t != null) {
        tables.add(t);
      }
      pos = end + 4;
    }
    return tables;
  }

  /**
    <td>
        <a href="machine.cgi?gid=2539&puid=45180">"300"</a>
    </td>
    <td>D. Gottlieb & Company</td>
    <td>August, 1975</td>
    <td>4</td>
    <td>EM</td>
    <td>Sports - Bowling</td>
  */
  private IpdbTable parseTable(String table) {
    String[] parts = StringUtils.splitByWholeSeparatorPreserveAllTokens(table, "<td>");
    if (parts.length == 7) {
      try {
        IpdbTable t = new IpdbTable();
        t.setIpdbUrl(StringUtils.substringBetween(parts[1], "<a href=\"", "\">"));
        t.setName(StringUtils.substringBetween(parts[1], "\">", "</a>"));
        t.setManufacturer(parts[2].replace("</td>", "").trim());

        String year = parts[3].contains(", ") 
            ? StringUtils.substringBetween(parts[3], ", ", "</td>").trim()
            : parts[3].replace("</td>", "").trim();
        if (StringUtils.isNotBlank(year)) {
          t.setYear(Integer.parseInt(year));
        }
        String players = parts[4].replace("</td>", "").trim();
        if (StringUtils.isNotBlank(players)) {
          t.setPlayers(Integer.parseInt(players));
        }
        t.setType(parts[5].replace("</td>", "").trim());
        t.setTheme(parts[6].replace("</td>", "").trim());
        return t;
      }
      catch (Exception e) {
        LOG.error("Error while parsing table : {}", e.getMessage());
      }
    }
    return null;
  }
}
