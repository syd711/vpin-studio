package de.mephisto.vpin.commons.utils.controller;

import net.java.games.input.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GameController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static GameController INSTANCE;

  private final List<GameControllerInputListener> listeners = new ArrayList<>();

  private boolean running = true;

  private GameController() {
    new Thread(() -> {
      Controller[] controllers = null;
      try {
        DirectInputEnvironmentPlugin plugin = new DirectInputEnvironmentPlugin();
        controllers = plugin.getControllers();
      }
      catch (UnsatisfiedLinkError e) {
        LOG.info("Loaded game controllers: " + e.getMessage());
      }
      List<Controller> filteredControllers = Arrays.stream(controllers)
          .filter(c -> !(c.getType().equals(Controller.Type.MOUSE)))
          .collect(Collectors.toList());

      if (filteredControllers.isEmpty()) {
        LOG.info("GameController did not resolve any game controllers, skipping initialization.");
        return;
      }

      LOG.info("Starting GameController");
      Event event = new Event();
      while (running) {
        for (Controller controller : filteredControllers) {
          if (controller.getType().equals(Controller.Type.MOUSE)) {
            continue;
          }

          controller.poll();
          EventQueue queue = controller.getEventQueue();
          while (queue.getNextEvent(event)) {
            Component comp = event.getComponent();
            if (comp.isAnalog()) {
              continue;
            }

            float value = event.getValue();
            if (value == 1) {
//              LOG.info("GameControllerEvent: " + comp.getName());
//              LOG.info("GameControllerEvent2: " + comp.getPollData());
              new Thread(() -> {
                for (GameControllerInputListener listener : new ArrayList<>(listeners)) {
                  listener.controllerEvent(comp.getName());
                }
              }).start();
            }
          }
        }

        try {
          Thread.sleep(50);
        }
        catch (InterruptedException e) {
          LOG.error("Error in game controller wait");
        }
      }
    }, "Game Controller").start();

  }

  public static GameController getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new GameController();
    }
    return INSTANCE;
  }

  public void shutdown() {
    running = false;
  }

  public void addListener(GameControllerInputListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(GameControllerInputListener listener) {
    this.listeners.remove(listener);
  }
}
