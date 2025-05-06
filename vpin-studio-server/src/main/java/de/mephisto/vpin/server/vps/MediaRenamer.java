package de.mephisto.vpin.server.vps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.matcher.VpsAutomatcher;
import de.mephisto.vpin.connectors.vps.model.VpsTable;

/**
 * Small tool that takes a folder, match all filenames against VPS database 
 * and proper rename all files, adding a number when several files for same table
 */
public class MediaRenamer {

	private final static Logger LOG = LoggerFactory.getLogger(MediaRenamer.class);

  public static void main(String[] args) throws Exception {

	// adjust here !
	Path path = Path.of("C:/temp/_Logo");
				//Path.of("C:/temp/_Wheels/Style Gris");

	VPS vpsDatabase = new VPS();
    vpsDatabase.reload();

	MediaRenamer renamer = new MediaRenamer();
	renamer.renameAll(path, vpsDatabase);
  }

  public void renameAll(Path path, VPS vpsDatabase) {

		VpsAutomatcher automatcher = VpsAutomatcher.getInstance();

		try {
			Files.list(path)
				.filter(p -> !Files.isDirectory(p) )
				.forEach(p -> {
					String filename = FilenameUtils.getBaseName(p.getFileName().toString());
					VpsTable table = automatcher.autoMatchTable(vpsDatabase, filename);
					if (table != null) {				
						renameToTable(p, table);
					}
					else {
						LOG.warn(" >>> No Match for " + p.getFileName());
					}
				});
			}
		catch(IOException ioe) {
			LOG.error("Error while listing files of " + path, ioe);
		}
	}	
	
	/**
	 * Rename 
	 * @param p The file that comes from the folder 
	 * @param table The closest table
	 */
	protected void renameToTable(Path p, VpsTable table) {

		String ext = "." + StringUtils.substringAfterLast(p.toString(), ".").toLowerCase();
		String tableName = table.getDisplayName();
		
		LOG.info("Match " + p.getFileName() + " with " + tableName);

		int i = 1;
		Path newPath = p.resolveSibling(tableName + ext);
		while (!p.equals(newPath) && Files.exists(newPath)) {
			newPath = p.resolveSibling(tableName + (i<10 ? "0" + i : i) + ext);
			i++;
		}
		// don't rename if already good
		if (!p.equals(newPath)) {
			try {
				Files.move(p, newPath);
			}
			catch (IOException ioe) {
				LOG.error("Cannot move file " + p.toString(), ioe);
			}
		}
	}

}