package de.mephisto.vpin.server.score;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple Analyser that dump recognized text in a file
 */
public class DMDScoreAnalyserLCD5 implements DMDScoreProcessor {
  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreAnalyserLCD5.class);

  private int ballsPlayed = 0;

  private boolean gameRunning = false;

  @Override
  public void onFrameReceived(Frame frame, List<FrameText> texts) {
    // concat texts

    String txts = "";
    for (FrameText text : texts) {
      txts += text.getText();
    }
    // txts is a series of 7 figures x nb of players 
    // then 2 figures for ball in play 
    // then 2 figures for credits 
    // other combinations can be ignored
    if (txts.length() < 7 + 4 || (txts.length() - 4) % 7 > 0) {
      return;
    }

    // when done, figures for balls turns for match from 00 to  90 
    int ballInPlay = Integer.parseInt(StringUtils.substring(txts, -4, -2));

    if (ballInPlay == 1 && !gameRunning)  {
      int nbPlayers = (txts.length() - 4) / 7;
      int score = 0;
      for (int i = 0; i < nbPlayers; i++) {
        score += Integer.parseInt(txts.substring(7 * i, 7 * i + 7));
      }
      if (score == 0) {
        ballsPlayed = 1;
        gameRunning = true;
        LOG.info("Game started, ball {} in play", ballsPlayed);
      }
    }
    else if (gameRunning && ballInPlay < 10 && ballsPlayed == ballInPlay - 1) {
      ballsPlayed = ballInPlay;
      LOG.info("ball {} in play", ballsPlayed);
    }
    else if (gameRunning && ballInPlay % 10 == 0) {
      // game over detected
      gameRunning = false;
      LOG.info("Game over");

      int nbPlayers = (txts.length() - 4) / 7;
      for (int i = 0; i < nbPlayers; i++) {
        int score = Integer.parseInt(txts.substring(7 * i, 7 * i + 7));
        LOG.info("Score player{} : {}", i + 1, score);
      }
    }
  }
}
