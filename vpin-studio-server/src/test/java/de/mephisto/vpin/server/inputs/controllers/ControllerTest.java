package de.mephisto.vpin.server.inputs.controllers;

import net.java.games.input.*;

public class ControllerTest {

  //@Test
  public void testControllers() throws InterruptedException {
    while (true) {
      /* Get the available controllers */
      Controller[] controllers = ControllerEnvironment
          .getDefaultEnvironment().getControllers();
      if (controllers.length == 0) {
        System.out.println("Found no controllers.");
        System.exit(0);
      }

      for (int i = 0; i < controllers.length; i++) {
        if (controllers[i].getType().equals(Controller.Type.MOUSE)) {
          continue;
        }
        if (controllers[i].getType().equals(Controller.Type.KEYBOARD)) {
          continue;
        }

        /* Remember to poll each one */
        controllers[i].poll();
        EventQueue queue = controllers[i].getEventQueue();
        Event event = new Event();
        while (queue.getNextEvent(event)) {
          Component comp = event.getComponent();
          if(comp.isAnalog()) {
            continue;
          }

          float value = event.getValue();
          if(value == 1) {
            System.out.println(comp);
          }
        }
      }

      /*
       * Sleep for 20 milliseconds, in here only so the example doesn't
       * thrash the system.
       */
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
