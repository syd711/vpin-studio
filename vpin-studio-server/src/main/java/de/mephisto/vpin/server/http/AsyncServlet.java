package de.mephisto.vpin.server.http;

import de.mephisto.vpin.server.GameInfo;
import de.mephisto.vpin.server.VPinService;
import de.mephisto.vpin.server.VPinServiceException;
import de.mephisto.vpin.server.popper.PopperManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AsyncServlet extends HttpServlet {
  private final static Logger LOG = LoggerFactory.getLogger(AsyncServlet.class);

  private final static String STATUS_ERROR = "ERROR";
  private final static String STATUS_OK = "OK";
  private final static String STATUS_TABLE_NOT_FOUND = "Table not found";

  private final static String PATH_LAUNCH = "/gameLaunch";
  private final static String PATH_EXIT = "/gameExit";

  public final static String PATH_SYSTEM_EXIT = "/systemExit";
  public final static String PATH_PING = "/ping";

  private final PopperManager popperManager;

  public AsyncServlet(PopperManager popperManager) {
    this.popperManager = popperManager;
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//    try {
//      String contextPath = request.getPathInfo();
//      String table = request.getParameter("table");
//      File tableFile = new File(table);
//      if (!StringUtils.isEmpty(table)) {
//        VPinService service = VPinService.create(true);
//        GameInfo game = service.getGameByFile(tableFile);
//        if (game == null) {
//          LOG.warn("No game found for name '" + tableFile.getName() + "' [" + request.getRequestURI() + "]");
//          this.writeResponse(request, response, STATUS_TABLE_NOT_FOUND);
//          return;
//        }
//
//        if (contextPath.equals(PATH_LAUNCH)) {
//          LOG.info("Received table launch cmd for '" + tableFile.getName() + "'");
//          popperManager.notifyTableStatusChange(game, true);
//        }
//        else if (contextPath.equals(PATH_EXIT)) {
//          LOG.info("Received table exit cmd for '" + tableFile.getName() + "'");
//          popperManager.notifyTableStatusChange(game, false);
//        }
//      }
//
//      writeResponse(request, response, STATUS_OK);
//    } catch (VPinServiceException e) {
//      LOG.error("Failed to execute POST: " + e.getMessage());
//      writeResponse(request, response, STATUS_ERROR);
//    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String contextPath = request.getPathInfo();
    if (contextPath.equals(PATH_SYSTEM_EXIT)) {
      LOG.info("Received system exit command.");
      System.exit(0);
    }
    else if (contextPath.equals(PATH_PING)) {
      writeResponse(request, response, STATUS_OK);
    }
  }

  private void writeResponse(HttpServletRequest request, HttpServletResponse response, String msg) throws IOException {
    ByteBuffer content = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
    AsyncContext async = request.startAsync();
    ServletOutputStream out = response.getOutputStream();
    out.setWriteListener(new WriteListener() {
      @Override
      public void onWritePossible() throws IOException {
        while (out.isReady()) {
          if (!content.hasRemaining()) {
            response.setStatus(200);
            async.complete();
            return;
          }
          out.write(content.get());
        }
      }

      @Override
      public void onError(Throwable t) {
        getServletContext().log("Async Error", t);
        async.complete();
      }
    });
  }
}