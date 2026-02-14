package de.mephisto.vpin.server.vps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.matcher.VpsAutomatcher;
import de.mephisto.vpin.connectors.vps.matcher.TableNameSplitter.TableNameParts;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.ipdb.IpdbDatabase;
import de.mephisto.vpin.server.ipdb.IpdbSettings;
import de.mephisto.vpin.server.ipdb.IpdbTable;

/**
 * Small tool that takes a folder, match all filenames against VPS database 
 * and proper rename all files, adding a number when several files for same table
 */
public class MediaRenamer {

	private final static Logger LOG = LoggerFactory.getLogger(MediaRenamer.class);

  public static void main(String[] args) throws Exception {

	// adjust here !
	Path path = Path.of("D:/Pincab Assets/_LOGO");
	Path target = Path.of("D:/Pincab Assets/_LOGO2");

	MediaRenamer renamer = new MediaRenamer();
	renamer.renameAll(path, target, false);
  }

  public void renameAll(Path path, Path target, boolean addExtras) {

		VpsAutomatcher automatcher = new VpsAutomatcher(null);

		// USE ONE DATABASE OR ANOTHER
		boolean useIpdb = false;

		final Function<TableNameParts, String> finder;
		if (useIpdb) {
			//FIXME Move in preferences
			IpdbSettings settings = new IpdbSettings();
			settings.setLogin("xxxx");
			settings.setPassword("xxxx");
			finder = useIpdb(settings);
		}
		else {
			finder = useVPS(automatcher);
		}

		StringBuilder undo = new StringBuilder();
		try {
			Files.list(path)
				.filter(p -> !Files.isDirectory(p) )
				.forEach(p -> {
					String filename = FilenameUtils.getBaseName(p.getFileName().toString());
					TableNameParts parts = automatcher.parseFilename(filename);
					String name = finder.apply(parts);
					
					if (name != null) {
						name = StringUtils.replace(name, "\"", "");
						name = StringUtils.replace(name, "/", "-");
						name = StringUtils.replace(name, ":", "-");

						renameToTable(p, target, path.relativize(p), name, parts, addExtras, undo);
					}
					else {
						LOG.warn(" >>> No Match for " + p.getFileName());
					}
				});

			}
		catch(IOException ioe) {
			LOG.error("Error while listing files of " + path, ioe);
		}
		finally {
			Path undotxt = path.resolve("undo.txt");
			try {
				Files.writeString(undotxt, undo);
			}
			catch (IOException ioe) {
			}
		}
	}	

	//--------------------------

	public Function<TableNameParts, String> useIpdb(IpdbSettings settings) {
		IpdbDatabase db = new IpdbDatabase(settings);
  	db.reload();

		return parts -> {
			List<IpdbTable> tables = db.find(parts.getTableName());
			if (tables.size() == 1) {
				IpdbTable table = tables.get(0);
				return table.getDisplayName();
			}
			else {
				return null;
			}
		};
	}

	public Function<TableNameParts, String> useVPS(VpsAutomatcher automatcher) {
		VPS vpsDatabase = new VPS();
  	vpsDatabase.reload();

		return parts -> {
			VpsTable table = automatcher.autoMatch(vpsDatabase, parts);
			return table != null ? table.getDisplayName() : null;
		};
	}

	//--------------------------

	/**
	 * Rename 
	 * @param p The file that comes from the folder 
	 * @param table The closest table
	 */
	protected void renameToTable(Path p, Path target, Path relativePath, String tableName, TableNameParts parts, boolean addExtras, StringBuilder undo) {

		String subfolder = relativePath.toFile().getParent();
		String ext = StringUtils.substringAfterLast(relativePath.toString(), ".").toLowerCase();
		
		LOG.info("Match " + relativePath.getFileName() + " with " + tableName);

		String fileName = tableName;
		if (addExtras && StringUtils.isNotEmpty(parts.getExtra())) {
			fileName += " " + parts.getExtra();
		}
		
		fileName = fileName.trim() + "." + ext;

		fileName = StringUtils.replace(fileName, "\\", "-");
		fileName = StringUtils.replace(fileName, "/", "-");
		fileName = StringUtils.replace(fileName, ":", "-");

		Path newPath = target;
		if (subfolder != null) {
			newPath = newPath.resolve(subfolder);
		}
		newPath = newPath.resolve(fileName);

		File newFile = FileUtils.uniqueAsset(newPath.toFile());
		newPath = newFile.toPath();

		// don't rename if same file
		if (!p.equals(newPath)) {
			try {
				Files.createDirectories(newPath.getParent());
				Files.move(p, newPath);
				undo.append("mv \"").append(newPath.toAbsolutePath()).append("\" \"").append(p.toAbsolutePath()).append("\";\n");
			}
			catch (IOException ioe) {
				LOG.error("Cannot move file " + p.toString(), ioe);
			}
		}
	}
}