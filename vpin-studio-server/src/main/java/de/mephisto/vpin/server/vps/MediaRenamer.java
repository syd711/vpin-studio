package de.mephisto.vpin.server.vps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.matcher.VpsAutomatcher;
import de.mephisto.vpin.connectors.vps.matcher.TableNameSplitter.TableNameParts;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.util.FileUtils;

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
	renamer.renameAll(path, path, vpsDatabase);
  }

  public void renameAll(Path path, Path target, VPS vpsDatabase) {

		VpsAutomatcher automatcher = new VpsAutomatcher(null);

		try {
			Files.list(path)
				.filter(p -> !Files.isDirectory(p) )
				.forEach(p -> {
					String filename = FilenameUtils.getBaseName(p.getFileName().toString());
					TableNameParts parts = automatcher.parseFilename(filename);
					VpsTable table = automatcher.autoMatch(vpsDatabase, parts);
					if (table != null) {				
						renameToTable(target, path.relativize(p), table, parts);
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
	protected void renameToTable(Path target, Path p, VpsTable table, TableNameParts parts) {

		String subfolder = target.toFile().getParent().toString();
		String ext = StringUtils.substringAfterLast(p.toString(), ".").toLowerCase();
		String tableName = table.getDisplayName();
		
		LOG.info("Match " + p.getFileName() + " with " + tableName);

		String fileName = tableName;
		if (StringUtils.isNotEmpty(parts.getExtra())) {
			fileName += " " + parts.getExtra();
		}
		fileName += "." + ext;

		fileName = StringUtils.replace(fileName, "\\", "-");
		fileName = StringUtils.replace(fileName, "/", "-");
		fileName = StringUtils.replace(fileName, ":", "-");

		Path newPath = target.resolve(subfolder).resolve(fileName);
		File newFile = FileUtils.uniqueAsset(newPath.toFile());
		newPath = newFile.toPath();

		// don't rename if same file
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