package de.mephisto.vpin.connectors.assets;

public class TableAsset {
  private String id;
  private String url;
  private String sourceId;
  private String mimeType;

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getMimeTypeSuffix() {
    if (mimeType != null) {
      String subType = mimeType.split("/")[1];
      if (subType.equalsIgnoreCase("jpeg")) {
        subType = "jpg";
      }
      return subType;
    }
    return null;
  }

  @Override
  public String toString() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TableAsset)) return false;

    TableAsset asset = (TableAsset) o;

    if (!id.equals(asset.id)) return false;
    return sourceId.equals(asset.sourceId);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + sourceId.hashCode();
    return result;
  }
}
