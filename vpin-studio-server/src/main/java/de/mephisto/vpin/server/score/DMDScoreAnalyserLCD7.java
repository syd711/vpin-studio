package de.mephisto.vpin.server.score;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple Analyser that dump recognized text in a file
 */
public class DMDScoreAnalyserLCD7 implements DMDScoreProcessor {
  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreAnalyserLCD5.class);

  private int ballsPlayed = 0;

  private int nbPlayers = 0;

  private Pattern gameOverPattern;
  
  private boolean gameRunning = false;

  private String scores = null;

  @Override
  public void onFrameReceived(Frame frame, List<FrameText> texts) {
    if (texts.size() == 0) {
      return;
    }

    scores = "";
    for (FrameText text : texts) {
      scores += text.getText() + " ";
    }

    // when done, figures for balls turns for match from 00 to  90 
    if (scores.contains("BALL 1") && !gameRunning)  {
      this.ballsPlayed = 1;
      this.gameRunning = true;
           
      // ex : "132510  BALL 1 00       00 "
      String[] playerScores = StringUtils.split(scores.replace("BALL 1", ""), " ");
      this.nbPlayers = playerScores.length;

      // Build the pattern used to detect game over
      String pattern = "";
      for (int i = 0; i < nbPlayers; i++) {
        pattern +=  (i > 0 ? "[ ]*" : "") + "[0-9]0";
      }
      this.gameOverPattern = Pattern.compile(pattern);

      LOG.info("Game started, ball {} in play, {} player(s)", ballsPlayed, nbPlayers);
    }
    else if (gameRunning && scores.contains("BALL " + (ballsPlayed + 1))) {
      // get scores
      // ex : "132510  BALL 1 00       00 "
      ///String[] scores = StringUtils.split(allTxts.replace("BALL " + (ballsPlayed + 1), ""), " ");
      //for (int i = 0; i < scores.length; i++) {
      //  int score = Integer.parseInt(scores[i]);
      //  LOG.info("Score player{} : {}", i + 1, score);
      //}
      this.ballsPlayed ++;
      LOG.info("ball {} in play", ballsPlayed);
    }
    else if (ballsPlayed==3 && gameOverPattern.matcher(texts.get(0).getText()).matches()) {
      // game over detected
      gameRunning = false;
      LOG.info("Game over");
    }
  }
}
