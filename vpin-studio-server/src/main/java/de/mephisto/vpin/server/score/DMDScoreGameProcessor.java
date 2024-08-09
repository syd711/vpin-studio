package de.mephisto.vpin.server.score;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMDScoreGameProcessor {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreGameProcessor.class);

  private GameToProcessorFactory factory = new GameToProcessorFactory();

  private String gameName;

  private boolean processing = false;

  private ExecutorService executor;

  private DMDScoreProcessor scanner;

  private DMDScoreProcessor analyser;

  private Frame previousFrame;


  public void setScanner(DMDScoreProcessor scanner) {
    this.scanner = scanner;
  }

  public void setAnalyser(DMDScoreProcessor analyser) {
    this.analyser = analyser;
  }

  public void processGameStart(String newGameName) {
    if (!StringUtils.equals(newGameName, gameName)) {
      // new game started, close previous one
      processGameStop();

      this.gameName = newGameName;

      // If not set, guess the scanner from rom and wrap it in a processing chain
      if (scanner == null) {
        scanner = factory.getScanner(gameName);
        if (scanner != null) {
          scanner = new DMDScoreProcessorDelegate(
            new DMDScoreProcessorFrameDump(),
            new DMDScoreProcessorFilterFixFrame(
              new DMDScoreProcessorImageDump(), 
              scanner
            )
          );
        }
      }

      if (analyser != null) {
        analyser = factory.getAnalyser(gameName);
      }

      //--------------
      // now processors are set, start the game

      if (scanner != null) {
        scanner.onFrameStart(gameName);
        // start the Executor that will process Frames
        executor = Executors.newFixedThreadPool(1);
        processing = true;

        if (analyser != null) {
          analyser.onFrameStart(gameName);
        }
      }
      LOG.info("Game name started : {}", gameName);
    }
  }

  public void processFrame(Frame frame) {
    if (previousFrame != null && frame.equals(previousFrame)) {
      LOG.info("Skipping duplicate frame {}", frame.getTimeStamp());
    }
    else {
      if (previousFrame != null) {
        previousFrame.setTimeStampClose(frame.getTimeStamp());

        final Frame frameToProcess = previousFrame;
        executor.execute(() -> {
          List<FrameText> texts = new ArrayList<>();
          scanner.onFrameReceived(frameToProcess, texts);
          if (analyser != null) {
            analyser.onFrameReceived(frameToProcess, texts);
          }
        });
      }
    }
    previousFrame = frame;
  }

  public void processGameStop() {
    if (gameName != null && scanner != null) {
      // stop executor and wait for 5min for all task to complete
      executor.shutdown();
      try {
        if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
          executor.shutdownNow();
        } 
      } catch (InterruptedException e) {
        executor.shutdownNow();
      }
      // now close the processor, pass a last texts as there can be remaining data to collect
      List<FrameText> texts = new ArrayList<>();
      if (scanner != null) {

        // always process the very last frame 
        if (previousFrame != null) {
          previousFrame.setTimeStampClose(Integer.MAX_VALUE);
          scanner.onFrameReceived(previousFrame, texts);
          if (analyser != null) {
            analyser.onFrameReceived(previousFrame, texts);
          }
        }

        processing = false;

        // now close processor and analyser
        scanner.onFrameStop(gameName);
        if (analyser != null) {
          analyser.onFrameStop(gameName);
        }
      }
      this.scanner = null;
      this.executor = null;
      this.analyser = null;
      this.gameName = null;
    }
  }

  public boolean isProcessing() {
    return processing;
  }
}