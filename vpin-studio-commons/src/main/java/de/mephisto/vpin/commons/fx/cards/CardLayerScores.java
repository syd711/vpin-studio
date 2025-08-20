package de.mephisto.vpin.commons.fx.cards;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * As we manipulate both dimension in template coordinate system and image coordinate, 
 * a WIDTH uppercase refer to template coordinate and width lowercase, refer to the image
 * then width = WIDTH * zoomX and height = HEIGHT * zoomY
 */
public class CardLayerScores extends Canvas implements CardLayer {

  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) throws Exception {
    double width = getWidth();
    double height = getHeight();
    GraphicsContext g = getGraphicsContext2D();
    g.clearRect(0, 0, width, height);

    // Build score blocks

    List<TextBlock> textBlocks = new ArrayList<>();
    TextBlock textBlock = new TextBlock(template);
    if (data != null) {
      if (template.isRawScore()) {
        addCardDataScoreFromRaw(textBlock, data.getRawScore());
      } else {
        List<ScoreRepresentation> scores = data.getScores();
        if (template.getMaxScores() > 0 && scores.size() > template.getMaxScores()) {
          scores = scores.subList(0, template.getMaxScores());
        }
        addCardDataScoreFromScoreList(textBlock, scores, template.isRenderPositions(), template.isRenderScoreDates());
      }
    }

    if (textBlock.isEmpty()) {
      return;
    }
    textBlocks.add(textBlock);

    //----------
    // Now render blocks

    double WIDTH = width / zoomX;
    double HEIGHT = height / zoomY;

    double fontSIZE = Math.max(template.getScoreFontSize(), 20);
    Font FONT = createFont(template.getScoreFontName(), template.getScoreFontStyle(), fontSIZE);

    //scale down block until every one is matching
    List<TextColumn> textColumns = null;
    do {
      for (TextBlock block : textBlocks) {
        while (block.getHEIGHT(FONT) > HEIGHT && fontSIZE > 20) {
          fontSIZE--;
          FONT = createFont(template.getScoreFontName(), template.getScoreFontStyle(), fontSIZE);
        }
      }
      textColumns = createTextColumns(template, textBlocks, FONT, HEIGHT);
    }
    while (textColumns.size() > 1 && fontSIZE-- > 20);

    while (computeTotalWIDTH(textColumns, FONT) >= WIDTH && fontSIZE > 20) {
      fontSIZE--;
      FONT = createFont(template.getScoreFontName(), template.getScoreFontStyle(), fontSIZE);
    }

    //yStart = centerYToRemainingSpace(textColumns, yStart, remainingHeight);
    //int columnsWidth = getColumnsWidth(textColumns);
    //double remainingXSpace = width - columnsWidth;

    Font font = createFont(template.getScoreFontName(), template.getScoreFontStyle(), fontSIZE * zoomY);
    g.setFont(font);
    g.setFill(Paint.valueOf(template.getFontColor()));
    g.setTextBaseline(VPos.CENTER);

    // one line added in columns, so put half on top and half on bottom
    double x =  0;
    double y = font.getSize() / 2;
    for (TextColumn textColumn : textColumns) {
      textColumn.renderAt(g, x, y, template.getRowMargin() * zoomY);
      x += textColumn.getWIDTH(FONT) * zoomX;
    }
  }

  //----------------------------------------------------

  /*private int getColumnsWidth(List<TextColumn> textColumns) {
    int width = 0;
    for (TextColumn textColumn : textColumns) {
      width = width + textColumn.getWidth();
    }
    return width;
  }*/


  private int computeTotalWIDTH(List<TextColumn> columns, Font FONT) {
    int width = 0;
    for (TextColumn column : columns) {
      width = width + column.getWIDTH(FONT);
    }
    return width;
  }

  List<TextColumn> createTextColumns(CardTemplate template, List<TextBlock> blocks, Font FONT, double remainingHEIGHT) {
    List<TextColumn> columns = new ArrayList<>();
    TextColumn column = new TextColumn();
    double columnHEIGHT = remainingHEIGHT;
    for (TextBlock block : blocks) {
      double HEIGHT = block.getHEIGHT(FONT);
      if (columnHEIGHT < HEIGHT) {
        columns.add(column);
        column = new TextColumn();
        columnHEIGHT = remainingHEIGHT;
      }

      columnHEIGHT = columnHEIGHT - HEIGHT;
      column.addBlock(block);
    }
    columns.add(column);
    return columns;
  }

  static class TextColumn {
    private final List<TextBlock> blocks = new ArrayList<>();

    TextColumn() {
    }

    public void addBlock(TextBlock block) {
      this.blocks.add(block);
    }

    public int getWIDTH(Font FONT) {
      int WIDTH = 0;
      for (TextBlock block : blocks) {
        WIDTH = Math.max(block.getWIDTH(FONT), WIDTH);
      }
      return WIDTH;
    }


    public void renderAt(GraphicsContext g, double x, double y, double rowMargin) {
      double startY = y;
      for (TextBlock block : blocks) {
        startY = block.renderAt(g, x, startY, rowMargin);
      }
    }

    public double getHEIGHT(Font FONT) {
      double HEIGHT = 0;
      for (TextBlock block : blocks) {
        HEIGHT = HEIGHT + block.getHEIGHT(FONT);
      }
      return HEIGHT;
    }
  }

  class TextBlock {
    private final List<String> lines;
    private final List<Boolean> externals;
    private final CardTemplate template;

    TextBlock(CardTemplate template) {
      this.lines = new ArrayList<>();
      this.externals = new ArrayList<>();
      this.template = template;
    }

    public void addLine(String line, boolean isExternal) {
      this.lines.add(line);
      this.externals.add(isExternal);
    }

    public double renderAt(GraphicsContext g, double x, double y, double rowMargin) {

      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        boolean external = externals.get(i);

        //we add a whitespace for every line, nicer formatting for multi-column
        if (external) {
          g.setFill(Paint.valueOf(template.getFriendsFontColor()));
        }
        else {
          g.setFill(Paint.valueOf(template.getFontColor()));
        }

        double fontSize = g.getFont().getSize();
        g.fillText(line, x, y + fontSize * 0.45);
        y = y + fontSize + rowMargin;
      }
      return y;
    }

    public double getHEIGHT(Font FONT) {
      // +1 to render an extra blank line with space distributed above and below
      return (this.lines.size() + 1) * (FONT.getSize() + template.getRowMargin()); 
    }

    public int getWIDTH(Font FONT) {
      int maxWIDTH = 0;
      for (String line : lines) {
        maxWIDTH = Math.max(maxWIDTH, getTextWidth(line + "   ", FONT));
      }
      return maxWIDTH;
    }

    protected int getTextWidth(String text, Font font) {
      Text theText = new Text(text);
      theText.setFont(font);
      return (int) theText.getBoundsInLocal().getWidth();
    }

    public boolean isEmpty() {
      return lines.isEmpty();
    }
  }

/*

    if (summary != null) {
      cardData.setRawScore(summary.getRaw());
      List<String> scores = template.isRawScore() ? 
          getCardDataScoreFromRaw(summary): 
          getCardDataScoreFromScoreList(summary, template.isRenderPositions(), template.isRenderScoreDates());
      cardData.setScores(scores);
    }

*/

  private void addCardDataScoreFromRaw(TextBlock text, String raw) {
    if (raw != null) {
      String formattedRaw = ScoreFormatUtil.formatRaw(raw);
      for (String line : formattedRaw.split("\n")) {
        if (StringUtils.isNotEmpty(line)) {
          text.addLine(line, false);
        }
      }
    }
  }

  public void addCardDataScoreFromScoreList(TextBlock text, List<ScoreRepresentation> scores, boolean renderPositions, boolean renderDate) {
    //calc max length of scores
    int scoreLength = 0;
    int initialsLength = 0;
    int maxPosition = 0;
    for (ScoreRepresentation score : scores) {
      scoreLength = Math.max(scoreLength, score.getFormattedScore().length());
      initialsLength = Math.max(initialsLength, score.getPlayerInitials().length());
      maxPosition = Math.max(maxPosition, score.getPosition());
    }
    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

    for (ScoreRepresentation score : scores) {
      String renderString = "";
      if (renderPositions) {
        renderString += StringUtils.leftPad(Integer.toString(score.getPosition()), maxPosition > 9 ? 2 : 1);
        renderString += ". ";
      }

      renderString += StringUtils.rightPad(score.getPlayerInitials(), initialsLength);
      renderString += "   ";

      String scoreText = StringUtils.leftPad(score.getFormattedScore(), scoreLength);
      renderString += scoreText;

      if (renderDate && score.hasPlayer() && score.getCreatedAt() != null) {
        renderString += "  ";
        renderString += df.format(score.getCreatedAt());
      }

      text.addLine(renderString, score.isExternal());
    }
  }
}
