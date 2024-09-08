package de.mephisto.vpin.server.exporter;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "export")
public class ExporterResource {

  @Autowired
  private DataExporterService exporterService;

  @Autowired
  private HighscoreExportService highscoreExportService;

  @RequestMapping(method = RequestMethod.GET, path = "/data", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody byte[] export(@RequestParam Map<String, String> customQuery, HttpServletResponse response) throws Exception {
    OutputStream output = response.getOutputStream();
    response.reset();
    response.setHeader("Content-disposition", "attachment; filename=export.xls");
    response.setContentType("application/msexcel");

    String export = exporterService.export(customQuery);
    output.write(export.getBytes());
    output.close();

    return IOUtils.toByteArray(new ByteArrayInputStream(export.getBytes()));
  }

  @RequestMapping("/data/plain")
  public String exportPlain(@RequestParam Map<String, String> customQuery, HttpServletResponse response) throws Exception {
    response.setContentType("text/csv");
    return exporterService.export(customQuery);
  }

  @RequestMapping(method = RequestMethod.GET, path = "/highscores", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody byte[] exportScores(@RequestParam Map<String, String> customQuery, HttpServletResponse response) throws Exception {
    OutputStream output = response.getOutputStream();
    response.reset();
    response.setHeader("Content-disposition", "attachment; filename=export.xls");
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

}
