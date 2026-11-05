package de.mephisto.vpin.ui.tables.dialogs.bulk;

/**
 * Where the imported files are placed below the move target folder.
 */
public enum BulkImportMoveStructure {
  /**
   * All files directly into the target folder.
   */
  FLAT,
  /**
   * The subfolders of the scanned source folder are recreated.
   */
  KEEP_STRUCTURE,
  /**
   * One subfolder per table: an existing subfolder matching the table is used, otherwise one named after the table
   * is created.
   */
  TABLE_FOLDER
}
