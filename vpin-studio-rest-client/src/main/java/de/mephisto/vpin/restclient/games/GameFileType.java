package de.mephisto.vpin.restclient.games;

/**
 * The files and folders of a table that can be opened in the file explorer, see the "{id}/fileinfo/{type}" endpoint.
 * The server resolves them, as only it knows the file layout of the table (shared folders or VPX 10.8.1 per table).
 */
public enum GameFileType {
  TABLE,
  DIRECTB2S,
  POV,
  DMD,
  PUPPACK,
  HIGHSCORE
}
