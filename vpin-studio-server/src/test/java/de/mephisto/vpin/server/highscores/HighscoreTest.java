package de.mephisto.vpin.server.highscores;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HighscoreTest {

  @Test
  public void testSingleChange() {
    HighscoreService highscoreService = new HighscoreService();

    List<Score> oldScores = new ArrayList<>();
    oldScores.add(new Score(new Date(), -1, "AAA", null, "9000", 9000, 1));
    oldScores.add(new Score(new Date(), -1, "BBB", null, "8000", 8000, 2));
    oldScores.add(new Score(new Date(), -1, "CCC", null, "7000", 7000, 3));
    oldScores.add(new Score(new Date(), -1, "DDD", null, "6000", 6000, 4));
    oldScores.add(new Score(new Date(), -1, "EEE", null, "5000", 5000, 5));

    List<Score> newScores = new ArrayList<>();
    newScores.add(new Score(new Date(), -1, "AAA", null, "9000", 9000, 1));
    newScores.add(new Score(new Date(), -1, "BBB", null, "8000", 8000, 2));
    newScores.add(new Score(new Date(), -1, "CCC", null, "7000", 7000, 3));
    newScores.add(new Score(new Date(), -1, "DDD", null, "6000", 6000, 4));
    newScores.add(new Score(new Date(), -1, "FFF", null, "5001", 5001, 5));

    List<Integer> changedPositions = highscoreService.calculateChangedPositions(oldScores, newScores);
    assertTrue(changedPositions.size() == 1);
    assertTrue(changedPositions.get(0).equals(5));
  }

  @Test
  public void testChange1() {
    HighscoreService highscoreService = new HighscoreService();

    List<Score> oldScores = new ArrayList<>();
    oldScores.add(new Score(new Date(), -1, "AAA", null, "9000", 9000, 1));
    oldScores.add(new Score(new Date(), -1, "BBB", null, "8000", 8000, 2));
    oldScores.add(new Score(new Date(), -1, "CCC", null, "7000", 7000, 3));
    oldScores.add(new Score(new Date(), -1, "DDD", null, "6000", 6000, 4));
    oldScores.add(new Score(new Date(), -1, "EEE", null, "5000", 5000, 5));

    List<Score> newScores = new ArrayList<>();
    newScores.add(new Score(new Date(), -1, "AAA", null, "9001", 9001, 1));
    newScores.add(new Score(new Date(), -1, "AAA", null, "9000", 9000, 2));
    newScores.add(new Score(new Date(), -1, "BBB", null, "8000", 8000, 3));
    newScores.add(new Score(new Date(), -1, "CCC", null, "7000", 7000, 4));
    newScores.add(new Score(new Date(), -1, "DDD", null, "6000", 6000, 5));

    List<Integer>  changedPositions = highscoreService.calculateChangedPositions(oldScores, newScores);
    assertTrue(changedPositions.size() == 1);
    assertTrue(changedPositions.get(0).equals(1));
  }

  @Test
  public void testChange2() {
    HighscoreService highscoreService = new HighscoreService();

    List<Score> oldScores = new ArrayList<>();
    oldScores.add(new Score(new Date(), -1, "AAA", null, "9000", 9000, 1));
    oldScores.add(new Score(new Date(), -1, "BBB", null, "8000", 8000, 2));
    oldScores.add(new Score(new Date(), -1, "CCC", null, "7000", 7000, 3));
    oldScores.add(new Score(new Date(), -1, "DDD", null, "6000", 6000, 4));
    oldScores.add(new Score(new Date(), -1, "EEE", null, "5000", 5000, 5));

    List<Score> newScores = new ArrayList<>();
    newScores.add(new Score(new Date(), -1, "AAA", null, "9001", 9001, 1));
    newScores.add(new Score(new Date(), -1, "AAA", null, "9000", 9000, 2));
    newScores.add(new Score(new Date(), -1, "BBB", null, "8000", 8000, 3));
    newScores.add(new Score(new Date(), -1, "CCC", null, "7000", 7000, 4));
    newScores.add(new Score(new Date(), -1, "DDD", null, "6001", 6001, 5));

    List<Integer>  changedPositions = highscoreService.calculateChangedPositions(oldScores, newScores);
    assertTrue(changedPositions.size() == 2);
    assertTrue(changedPositions.get(0).equals(1));
    assertTrue(changedPositions.get(1).equals(5));
  }

  @Test
  public void testChange3() {
    HighscoreService highscoreService = new HighscoreService();

    List<Score> oldScores = new ArrayList<>();
    oldScores.add(new Score(new Date(), -1, "AAA", null, "9000", 9000, 1));
    oldScores.add(new Score(new Date(), -1, "BBB", null, "8000", 8000, 2));
    oldScores.add(new Score(new Date(), -1, "CCC", null, "7000", 7000, 3));
    oldScores.add(new Score(new Date(), -1, "DDD", null, "6000", 6000, 4));
    oldScores.add(new Score(new Date(), -1, "EEE", null, "5000", 5000, 5));

    List<Score> newScores = new ArrayList<>();
    newScores.add(new Score(new Date(), -1, "AAA", null, "9003", 9003, 1));
    newScores.add(new Score(new Date(), -1, "AAA", null, "9002", 9002, 2));
    newScores.add(new Score(new Date(), -1, "AAA", null, "9001", 9001, 3));
    newScores.add(new Score(new Date(), -1, "BBB", null, "8000", 8000, 4));
    newScores.add(new Score(new Date(), -1, "CCC", null, "7000", 7000, 5));

    List<Integer>  changedPositions = highscoreService.calculateChangedPositions(oldScores, newScores);
    assertTrue(changedPositions.size() == 3);
    assertTrue(changedPositions.get(0).equals(1));
    assertTrue(changedPositions.get(1).equals(2));
    assertTrue(changedPositions.get(2).equals(3));
  }
}
