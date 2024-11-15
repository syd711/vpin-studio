package de.mephisto.vpin.server.frontend.popper;

import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PupServer {
  private final static Logger LOG = LoggerFactory.getLogger(PupServer.class);

  private final static int PORT = 8091;
  public static final String EXE_NAME = "PuPServer";

  private final PinUPConnector pinUPConnector;
  private final SystemService systemService;

  private RestTemplate restTemplate;
  private String baseUrl;

  public PupServer(PinUPConnector pinUPConnector, SystemService systemService) {
    this.pinUPConnector = pinUPConnector;
    this.systemService = systemService;

    baseUrl = "http://localhost:" + PORT + "/";
    restTemplate = new RestTemplate();
    List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setPrettyPrint(true);
    converter.getObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
    messageConverters.add(converter);
    restTemplate.setMessageConverters(messageConverters);
  }

  public void launchGame(int gameId) {
    if (!isRunning()) {
      launchServer();
    }

    final RestTemplate plainTemplate = new RestTemplate();
    plainTemplate.getForObject(baseUrl + "function/launchgame/" + gameId, String.class);
  }

  public boolean isRunning() {
    return systemService.isProcessRunning(EXE_NAME);
  }

  public void startServer() {
    launchServer();
  }

  public void stopServer() {
    systemService.killProcesses(EXE_NAME);
  }

  private void launchServer() {
    if (systemService.isProcessRunning("PupServer")) {
      LOG.info("Can't launch PupServer, already running.");
      return;
    }

    LOG.info("Launching PuPServer on port " + PORT);
    File installationFolder = pinUPConnector.getInstallationFolder();
    List<String> params = Arrays.asList("PuPServer.exe", "-wwwport", String.valueOf(PORT), "-sockport", "8888");
    SystemCommandExecutor executor = new SystemCommandExecutor(params, true);
    executor.setDir(installationFolder);
    executor.executeCommandAsync();

    StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
    StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
    if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
      LOG.error("PupServer.exe failed: {}", standardErrorFromCommand);
    }

    boolean b = systemService.waitForProcess(EXE_NAME, 5, 3000);
    if(b) {
      LOG.info("Found server process: {}", EXE_NAME);
    }
    else {
      LOG.error("Server process not found: {}", EXE_NAME);
    }
  }
}
