package de.mephisto.vpin.ui.vps;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.connectors.vps.model.VpsAuthoredUrls;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.ui.vps.VpsTablesController.VpsTableModel;

class VpsTablesPredicateFactory {

  private String[] tableFormats;

  private boolean installedOnly;
  private boolean notInstalledOnly;

  private LocalDate lastUpdateDate;

  private String[] authors;
  private boolean searchAuthorInOtherAssetsToo;

  private String[] manufacturers;
  private String theme;

  private boolean vpsUpdates;

  private boolean withBackglass;
  private boolean withPupPack;
  private boolean withRom;
  private boolean withTopper;
  private boolean withWheel;
  private boolean withAltSound;
  private boolean withAltColor;
  private boolean withTutorial;
  private boolean withPov;

  private LinkedHashMap<String, Boolean> features = new LinkedHashMap<>();

  public void setTableFormats(String[] tableFormats) {
    this.tableFormats = tableFormats;
  }

  public boolean isInstalledOnly() {
    return installedOnly;
  }

  public void setInstalledOnly(boolean installedOnly) {
    this.installedOnly = installedOnly;
  }

  public boolean isNotInstalledOnly() {
    return notInstalledOnly;
  }

  public void setNotInstalledOnly(boolean notInstalledOnly) {
    this.notInstalledOnly = notInstalledOnly;
  }

  public LocalDate getLastUpdateDate() {
    return lastUpdateDate;
  }

  public void setLastUpdateDate(LocalDate lastUpdateDate) {
    this.lastUpdateDate = lastUpdateDate;
  }

  public String getAuthor() {
    return StringUtils.join(authors, ", ");
  }

  public void setAuthor(String author) {
    this.authors = splitAndTrim(author, ",;");
  }

  public boolean isSearchAuthorInOtherAssetsToo() {
    return searchAuthorInOtherAssetsToo;
  }

  public void setSearchAuthorInOtherAssetsToo(boolean searchAuthorInOtherAssetsToo) {
    this.searchAuthorInOtherAssetsToo = searchAuthorInOtherAssetsToo;
  }

  public String getManufacturer() {
    return StringUtils.join(manufacturers, ", ");
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturers = splitAndTrim(manufacturer, ",;");
  }

  public String getTheme() {
    return theme;
  }

  public void setTheme(String theme) {
    this.theme = theme;
  }


  public boolean isWithTutorial() {
    return withTutorial;
  }

  public void setWithTutorial(boolean withRes) {
    this.withTutorial = withRes;
  }

  public boolean isWithPov() {
    return withPov;
  }

  public void setWithPov(boolean withPov) {
    this.withPov = withPov;
  }

  public boolean isWithRom() {
    return withRom;
  }

  public void setWithRom(boolean withPov) {
    this.withRom = withPov;
  }

  public boolean isWithTopper() {
    return withTopper;
  }

  public void setWithTopper(boolean withIni) {
    this.withTopper = withIni;
  }

  public boolean isWithBackglass() {
    return withBackglass;
  }

  public void setWithBackglass(boolean withBackglass) {
    this.withBackglass = withBackglass;
  }

  public boolean isWithPupPack() {
    return withPupPack;
  }

  public void setWithPupPack(boolean withPupPack) {
    this.withPupPack = withPupPack;
  }

  public boolean isWithAltSound() {
    return withAltSound;
  }

  public void setWithAltSound(boolean withAltSound) {
    this.withAltSound = withAltSound;
  }

  public boolean isWithAltColor() {
    return withAltColor;
  }

  public void setWithAltColor(boolean withAltColor) {
    this.withAltColor = withAltColor;
  }

  public boolean isWithWheel() {
    return withWheel;
  }

  public void setWithWheel(boolean withWheel) {
    this.withWheel = withWheel;
  }

  public boolean isVpsUpdates() {
    return vpsUpdates;
  }

  public void setVpsUpdates(boolean vpsUpdates) {
    this.vpsUpdates = vpsUpdates;
  }

  public void registerFeature(String feature) {
    features.put(feature, Boolean.FALSE);
  }

  public boolean isSelectedFeature(String feature) {
    return features.containsKey(feature) && features.get(feature);
  }

  public boolean toggleFeature(String feature) {
    if (features.containsKey(feature)) {
      boolean newstate = !features.get(feature);
      features.put(feature, newstate);
      return newstate;
    }
    return false;
  }

  public boolean isResetted() {
    return isFeaturesFilterEmpty()
        && lastUpdateDate == null
        && (authors == null || authors.length == 0)
        && (manufacturers == null || manufacturers.length == 0)
        && StringUtils.isEmpty(theme)
        && !this.vpsUpdates
        && !installedOnly
        && !notInstalledOnly
        && !this.withAltColor
        && !this.withAltSound
        && !this.withBackglass
        && !this.withTopper
        && !this.withWheel
        && !this.withRom
        && !this.withTutorial
        && !this.withPupPack;
  }

  public boolean isFeaturesFilterEmpty() {
    boolean isFeaturesEmpty = true;
    for (String f : features.keySet()) {
      isFeaturesEmpty &= !features.get(f);
    }
    return isFeaturesEmpty;
  }

  /**
   * We need a new Predicate each time else TableView does not detect the changes
   */
  public Predicate<VpsTableModel> buildPredicate(boolean noVPX, String searchTerm) {
    return new Predicate<VpsTableModel>() {
      @Override
      public boolean test(VpsTableModel model) {
        VpsTable table = model.getVpsTable();

        if (tableFormats != null && !containsAnyIgnoreCase(table.getAvailableTableFormats(), tableFormats)) {
          return false;
        }

        if (installedOnly && !noVPX && !model.isInstalled()) {
          return false;
        }
        if (notInstalledOnly && !noVPX && model.isInstalled()) {
          return false;
        }

        if (lastUpdateDate != null) {
          LocalDate updated = Instant.ofEpochMilli(table.getUpdatedAt())
              .atZone(ZoneId.systemDefault()).toLocalDate();
          if (lastUpdateDate.compareTo(updated) > 0) {
            return false;
          }
        }

        if (StringUtils.isNotEmpty(searchTerm)
            && !StringUtils.containsIgnoreCase(table.getName(), searchTerm)
          //&& !StringUtils.containsIgnoreCase(table.getRom(), filterValue)
        ) {
          return false;
        }

        // check for feature
        for (String f : features.keySet()) {
          if (features.get(f)) {
            boolean hasFeature = containsIgnoreCase(table.getAllFeatures(), f);
            /*if (!hasFeature) {
              // check at version level
              for (VpsTableVersion version: table.getTableFiles()) {
                if (containsIgnoreCase(version.getFeatures(), f)) {
                  hasFeature = true;
                  break;
                }
              }  
            }*/
            if (!hasFeature) {
              return false;
            }
          }
        }

        if (!containsAnyIgnoreCase(table.getManufacturer(), manufacturers)) {
          return false;
        }
        if (!containsIgnoreCase(table.getTheme(), theme)) {
          return false;
        }

        if (isWithBackglass() && !VpsUtil.isDataAvailable(table.getB2sFiles())) {
          return false;
        }
        if (isWithPupPack() && !VpsUtil.isDataAvailable(table.getPupPackFiles())) {
          return false;
        }
        if (isWithRom() && !VpsUtil.isDataAvailable(table.getRomFiles())) {
          return false;
        }
        if (isWithTopper() && !VpsUtil.isDataAvailable(table.getTopperFiles())) {
          return false;
        }
        if (isWithWheel() && !VpsUtil.isDataAvailable(table.getWheelArtFiles())) {
          return false;
        }
        if (isWithAltSound() && !VpsUtil.isDataAvailable(table.getAltSoundFiles())) {
          return false;
        }
        if (isWithAltColor() && !VpsUtil.isDataAvailable(table.getAltColorFiles())) {
          return false;
        }
        if (isWithTutorial() && !VpsUtil.isDataAvailable(table.getTutorialFiles())) {
          return false;
        }
        if (isWithPov() && !VpsUtil.isDataAvailable(table.getPovFiles())) {
          return false;
        }

        // As this filter is a bit heavy, keep it last
        if (searchAuthorInOtherAssetsToo) {
          if (!containsAnyAuthor(table.getTableFiles(), authors)
              && !containsAnyAuthor(table.getB2sFiles(), authors)
              && !containsAnyAuthor(table.getPupPackFiles(), authors)
              && !containsAnyAuthor(table.getTopperFiles(), authors)
              && !containsAnyAuthor(table.getWheelArtFiles(), authors)
              && !containsAnyAuthor(table.getAltColorFiles(), authors)
              && !containsAnyAuthor(table.getAltSoundFiles(), authors)
              && !containsAnyAuthor(table.getTutorialFiles(), authors)
              && !containsAnyAuthor(table.getPovFiles(), authors)
          ) {
            return false;
          }
        }
        else {
          if (!containsAnyAuthor(table.getTableFiles(), authors)) {
            return false;
          }
        }

        // else not filtered
        return true;
      }
    };
  }

  public Predicate<VpsTableVersion> buildTableVersionPredicate() {
    return new Predicate<VpsTableVersion>() {
      @Override
      public boolean test(VpsTableVersion version) {

        if (tableFormats != null && !containsIgnoreCase(tableFormats, version.getTableFormat())) {
          return false;
        }

        for (String f : features.keySet()) {
          if (features.get(f)) {
            if (!containsIgnoreCase(version.getFeatures(), f)) {
              return false;
            }
          }
        }

        if (lastUpdateDate != null) {
          LocalDate updated = Instant.ofEpochMilli(version.getCreatedAt())
              .atZone(ZoneId.systemDefault()).toLocalDate();
          if (lastUpdateDate.compareTo(updated) > 0) {
            return false;
          }
        }

        if (!containsAnyAuthor(version, authors)) {
          return false;
        }

        return true;
      }
    };
  }

  public Predicate<VpsAuthoredUrls> buildAuthoredUrlsPredicate() {
    return new Predicate<VpsAuthoredUrls>() {
      @Override
      public boolean test(VpsAuthoredUrls url) {

        if (searchAuthorInOtherAssetsToo && !containsAnyAuthor(url, authors)) {
          return false;
        }

        return true;
      }
    };
  }

  protected boolean containsAnyIgnoreCase(String manufacturer, String[] _manufacturers) {
    if (_manufacturers == null || _manufacturers.length == 0) {
      return true;
    }
    for (String m : _manufacturers) {
      if (StringUtils.containsIgnoreCase(manufacturer, m)) {
        return true;
      }
    }
    return false;
  }

  protected boolean containsAnyIgnoreCase(List<String> values, String[] tosearch) {
    if (tosearch == null || tosearch.length == 0) {
      return true;
    }
    if (values != null) {
      for (String s : tosearch) {
        if (containsIgnoreCase(values, s)) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean containsIgnoreCase(String[] values, String value) {
    if (StringUtils.isEmpty(value)) {
      return true;
    }
    if (values != null) {
      for (String t : values) {
        if (StringUtils.containsIgnoreCase(t, value)) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean containsIgnoreCase(List<String> values, String value) {
    if (StringUtils.isEmpty(value)) {
      return true;
    }
    if (values != null) {
      for (String t : values) {
        if (StringUtils.containsIgnoreCase(t, value)) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean containsAnyAuthor(List<? extends VpsAuthoredUrls> urls, String[] _authors) {
    if (_authors == null || _authors.length == 0) {
      return true;
    }
    if (urls != null) {
      for (VpsAuthoredUrls url : urls) {
        if (url.getAuthors() != null) {
          for (String author : url.getAuthors()) {
            if (containsAnyIgnoreCase(author, _authors)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  protected boolean containsAnyAuthor(VpsAuthoredUrls url, String[] _authors) {
    if (_authors == null || _authors.length == 0) {
      return true;
    }
    if (url != null) {
      if (url.getAuthors() != null) {
        for (String author : url.getAuthors()) {
          if (containsAnyIgnoreCase(author, _authors)) {
            return true;
          }
        }
      }
    }
    return false;
  }


  protected String[] splitAndTrim(String input, String separators) {
    if (input != null) {
      String[] splitted = StringUtils.split(input, separators);
      for (int i = 0; i < splitted.length; i++) {
        splitted[i] = splitted[i].trim();
      }
      return splitted;
    }

    return null;
  }

}
