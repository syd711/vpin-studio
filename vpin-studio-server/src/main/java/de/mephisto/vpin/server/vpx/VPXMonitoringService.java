package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.server.games.GameStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class VPXMonitoringService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VPXMonitoringService.class);
  private final AtomicBoolean running = new AtomicBoolean(false);


  private Thread monitorThread;

  @Autowired
  private GameStatusService gameStatusService;


  public void stopMonitoring() {
    if (monitorThread != null) {
      this.running.set(false);
    }
  }

  public void startMonitor() {
//    this.running.set(true);
//    monitorThread = new Thread(() -> {
//      try {
//        Thread.currentThread().setName("VPX Monitor Thread");
//        LOG.info("VPX monitor started.");
//        while (running.get()) {
//          List<ProcessHandle> collect = ProcessHandle.allProcesses().filter(p -> p.info().command().isPresent()).collect(Collectors.toList());
//          for (ProcessHandle p : collect) {
//            System.out.println(p.info().command());
//            String cmdName = p.info().command().get().replaceAll("\\\\", "/");
//            if (cmdName.contains("/")) {
//              cmdName = cmdName.substring(cmdName.lastIndexOf("/") + 1);
//            }
//
//            if (cmdName.toLowerCase().contains("Visual Pinball".toLowerCase())) {
//              GameStatus status = gameStatusService.getStatus();
//              System.out.println(cmdName + " " + p.info().arguments());
//            }
//          }
//          Thread.sleep(3000);
//        }
//      } catch (Exception e) {
//        LOG.info("VPX monitor failed: " + e.getMessage(), e);
//      } finally {
//        LOG.info(Thread.currentThread().getName() + " terminated.");
//      }
//    });
//    monitorThread.start();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    startMonitor();
  }
}
