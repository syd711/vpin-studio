package de.mephisto.vpin.server.achievements.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code bundle.json} of the achievement rule bundle: the revision, where the ROMs came from and the platform files.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleBundleInfo {
  public static final ObjectMapper MAPPER = JsonMapper.builder()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .enable(SerializationFeature.INDENT_OUTPUT)
      .build();

  private int revision;
  private List<Source> sources = new ArrayList<>();
  private List<String> platforms = new ArrayList<>();

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public List<Source> getSources() {
    return sources;
  }

  public void setSources(List<Source> sources) {
    this.sources = sources != null ? sources : new ArrayList<>();
  }

  public List<String> getPlatforms() {
    return platforms;
  }

  public void setPlatforms(List<String> platforms) {
    this.platforms = platforms != null ? platforms : new ArrayList<>();
  }

  /**
   * One run of the {@link RuleBundleBuilder}: the map version the added ROMs were taken from.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Source {
    private int revision;
    private String maps;
    private int added;

    public Source() {
    }

    public Source(int revision, String maps, int added) {
      this.revision = revision;
      this.maps = maps;
      this.added = added;
    }

    public int getRevision() {
      return revision;
    }

    public void setRevision(int revision) {
      this.revision = revision;
    }

    /**
     * The version of the map repository, e.g. the commit id.
     */
    public String getMaps() {
      return maps;
    }

    public void setMaps(String maps) {
      this.maps = maps;
    }

    public int getAdded() {
      return added;
    }

    public void setAdded(int added) {
      this.added = added;
    }
  }
}
