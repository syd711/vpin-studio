package de.mephisto.vpin.server.exporter;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "export")
public class ExporterResource {

  @Autowired
  private TableExporterService exporterService;

  @Autowired
  private HighscoreExportService highscoreExportService;

  @Autowired
  private BackglassExportService backglassExportService;

  @Autowired
  private MediaExportService mediaExportService;

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/tables",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void export(
            @RequestParam Map<String, String> customQuery,
            @RequestParam(name = "filepath", required = false) String filepath,
            HttpServletResponse response) throws Exception {

        String export = exporterService.export(customQuery);
        byte[] data = export.getBytes();

        // -------------------------------------------
        // CASE 1: filepath IS PROVIDED → write to disk
        // -------------------------------------------
        if (filepath != null && !filepath.isBlank()) {

            // SECURITY: normalize and validate the path
            Path outputPath = Paths.get(filepath).normalize();

            // Write file to server
            Files.createDirectories(outputPath.getParent());  // ensure directory exists
            Files.write(outputPath, data);

            // Respond with confirmation message (NOT a file download)
           // response.reset();
          //  response.setContentType(MediaType.TEXT_PLAIN_VALUE);
           // response.getWriter().write("Exported to: " + outputPath.toAbsolutePath());
            return;
        }

        // ----------------------------------------------------
        // CASE 2: NO filepath → behave as before (browser download)
        // ----------------------------------------------------

        String defaultFilename = "export.xls";

        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + defaultFilename + "\"");
        response.setContentType("application/vnd.ms-excel");

        try (OutputStream out = response.getOutputStream()) {
            out.write(data);
        }
    }

    @RequestMapping("/tables/plain")
  public String exportPlain(@RequestParam Map<String, String> customQuery, HttpServletResponse response) throws Exception {
    response.setContentType("text/csv");
    return exporterService.export(customQuery);
  }

  @RequestMapping(method = RequestMethod.GET, path = "/highscores", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody byte[] exportScores(@RequestParam Map<String, String> customQuery, HttpServletResponse response) throws Exception {
    OutputStream output = response.getOutputStream();
    response.reset();
    response.setHeader("Content-disposition", "attachment; filename=highscores.xls");
    response.setContentType("application/msexcel");

    String export = highscoreExportService.export(customQuery);
    output.write(export.getBytes());
    output.close();

    return IOUtils.toByteArray(new ByteArrayInputStream(export.getBytes()));
  }

  @RequestMapping("/highscores/plain")
  public String exportScoresPlain(@RequestParam Map<String, String> customQuery, HttpServletResponse response) throws Exception {
    response.setContentType("text/csv");
    return highscoreExportService.export(customQuery);
  }

  @RequestMapping(method = RequestMethod.GET, path = "/backglasses", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody byte[] exportBackglasses(@RequestParam Map<String, String> customQuery, HttpServletResponse response) throws Exception {
    OutputStream output = response.getOutputStream();
    response.reset();
    response.setHeader("Content-disposition", "attachment; filename=backglasses.xls");
    response.setContentType("application/msexcel");

    String export = backglassExportService.export(customQuery);
    output.write(export.getBytes());
    output.close();

    return IOUtils.toByteArray(new ByteArrayInputStream(export.getBytes()));
  }

  @RequestMapping("/backglasses/plain")
  public String exportBackglassesPlain(@RequestParam Map<String, String> customQuery, HttpServletResponse response) throws Exception {
    response.setContentType("text/csv");
    return backglassExportService.export(customQuery);
  }

  @RequestMapping(method = RequestMethod.GET, path = "/media", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody byte[] exportMedia(@RequestParam Map<String, String> customQuery, HttpServletResponse response) throws Exception {
    OutputStream output = response.getOutputStream();
    response.reset();
    response.setHeader("Content-disposition", "attachment; filename=table-media.xls");
    response.setContentType("application/msexcel");

    String export = mediaExportService.export(customQuery);
    output.write(export.getBytes());
    output.close();

    return IOUtils.toByteArray(new ByteArrayInputStream(export.getBytes()));
  }

  @RequestMapping("/media/plain")
  public String exportMediaPlain(@RequestParam Map<String, String> customQuery, HttpServletResponse response) throws Exception {
    response.setContentType("text/csv");
    return mediaExportService.export(customQuery);
  }

}
