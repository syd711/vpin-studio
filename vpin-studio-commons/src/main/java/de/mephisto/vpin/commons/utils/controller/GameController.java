package de.mephisto.vpin.commons.utils.controller;

import net.java.games.input.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class GameController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static GameController INSTANCE;

  //modified concurrently: every event is dispatched on its own thread while the pause menu adds/removes itself
  private final List<GameControllerInputListener> listeners = new CopyOnWriteArrayList<>();

  private boolean running = true;

  private GameController() {
    new Thread(() -> {
      Controller[] controllers = new Controller[0];
      try {
        DirectInputEnvironmentPlugin plugin = new DirectInputEnvironmentPlugin();
        controllers = plugin.getControllers();
      }
      catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
        LOG.info("Loaded game controllers: " + e.getMessage());
      }
      List<Controller> filteredControllers = Arrays.stream(controllers)
          .filter(c -> !(c.getType().equals(Controller.Type.MOUSE)))
          .collect(Collectors.toList());

      if (filteredControllers.isEmpty()) {
        LOG.info("GameController did not resolve any game controllers, skipping initialization.");
        return;
      }

      LOG.info("GameController resolved {} controller(s):", filteredControllers.size());
      for (Controller controller : filteredControllers) {
        LOG.info("  - '{}' (type: {}, port: {}, components: {})", controller.getName(), controller.getType(), controller.getPortType(), controller.getComponents().length);
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
                for (GameControllerInputListener listener : listeners) {
                  try {
                    listener.controllerEvent(comp.getName());
                  }
                  catch (Exception e) {
                    LOG.error("Game controller listener {} failed for '{}': {}", listener.getClass().getSimpleName(), comp.getName(), e.getMessage(), e);
                  }
                }
              }, "Game Controller Event").start();
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
    //a listener registered twice would receive every event twice
    ((CopyOnWriteArrayList<GameControllerInputListener>) this.listeners).addIfAbsent(listener);
  }

  public void removeListener(GameControllerInputListener listener) {
    this.listeners.remove(listener);
  }
}
