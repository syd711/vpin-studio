package de.mephisto.vpin.ui.tables.dialogs.bulk;

/**
 * How an imported file is added to the existing media of its screen.
 */
public enum BulkImportMode {
  OVERWRITE("bulk_importer.mode_overwrite"),
  APPEND("bulk_importer.mode_append"),
  /**
   * The file is only imported when the screen has no media yet.
   */
  IF_MISSING("bulk_importer.mode_if_missing");

  private final String messageKey;

  BulkImportMode(String messageKey) {
    this.messageKey = messageKey;
  }

  public String getMessageKey() {
    return messageKey;
  }
}
