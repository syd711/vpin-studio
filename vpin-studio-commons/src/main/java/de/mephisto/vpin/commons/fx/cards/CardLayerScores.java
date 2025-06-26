package de.mephisto.vpin.commons.fx.cards;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public class CardLayerScores extends CardLayer {

  @Override
  protected void draw(GraphicsContext g, CardTemplate template, CardData data) throws Exception {

    // Build score blocks

    List<TextBlock> textBlocks = new ArrayList<>();
    TextBlock textBlock = new TextBlock(g, template);
    for (String score : data.getScores()) {
      boolean external = false;
      if (StringUtils.startsWith(score, CardData.MARKER_EXTERNAL_SCORE)) {
        external = true;
        score = StringUtils.removeStart(score, CardData.MARKER_EXTERNAL_SCORE);
      }
      textBlock.addLine(score, external);
    }

    if (!textBlock.isEmpty()) {
      textBlocks.add(textBlock);
    }

    //----------
    // Now render blocks

    double width = getWidth();
    double height = getHeight();

    int fontSize = (int) Math.floor(height / textBlocks.size());
    if (fontSize > template.getScoreFontSize()) {
      fontSize = template.getScoreFontSize();
    }
    else if (fontSize < 20) {
      fontSize = 20;
    }

    Font font = createFont(template.getScoreFontName(), template.getScoreFontStyle(), fontSize);
    g.setFont(font);

    g.setFill(Paint.valueOf(template.getFontColor()));
    g.setTextBaseline(VPos.CENTER);

    List<TextColumn> textColumns = createTextColumns(template, textBlocks, g, height);
    while (fontSize > 20 && textColumns.size() > 1) {
      int downScale = (int) (g.getFont().getSize() - 1);
      font = createFont(template.getScoreFontName(), template.getScoreFontStyle(), downScale);
      g.setFont(font);
      textColumns = createTextColumns(template, textBlocks, g, height);
    }

    scaleDownToWidth(template, width, g, textColumns);

    //yStart = centerYToRemainingSpace(textColumns, yStart, remainingHeight);
    int columnsWidth = getColumnsWidth(textColumns);
    double remainingXSpace = width - columnsWidth;

    // one line added in columns, so put half on top and half on bottom
    double x =  0;
    double y = g.getFont().getSize() / 2;
    for (TextColumn textColumn : textColumns) {
      textColumn.renderAt(x, y);
      x += textColumn.getWidth();
    }
  }

  //----------------------------------------------------

  private int getColumnsWidth(List<TextColumn> textColumns) {
    int width = 0;
    for (TextColumn textColumn : textColumns) {
      width = width + textColumn.getWidth();
    }
    return width;
  }

  private void scaleDownToWidth(CardTemplate template, double availableWidth, GraphicsContext g, List<TextColumn> textColumns) {
    int width = computeTotalWidth(textColumns);
    while (width >= availableWidth) {
      int fontSize = ((int) g.getFont().getSize()) - 1;
      Font font = createFont(template.getScoreFontName(), template.getScoreFontStyle(), fontSize);
      g.setFont(font);
      width = computeTotalWidth(textColumns);
    }
  }

  private int computeTotalWidth(List<TextColumn> columns) {
    int width = 0;
    for (TextColumn column : columns) {
      int columnWidth = column.getWidth();
      width = width + columnWidth;
    }
    return width;
  }

  List<TextColumn> createTextColumns(CardTemplate template, List<TextBlock> blocks, GraphicsContext g, double remainingHeight) {

    List<TextColumn> columns = new ArrayList<>();

    //scale down block until every one is matching
    for (TextBlock block : blocks) {
      while (block.getHeight() > remainingHeight) {
        int fontSize = ((int) g.getFont().getSize()) - 1;
        Font font = createFont(template.getScoreFontName(), template.getScoreFontStyle(), fontSize);
        g.setFont(font);
      }
    }

    TextColumn column = new TextColumn();
    double columnHeight = remainingHeight;
    for (TextBlock block : blocks) {
      double height = block.getHeight();
      if (columnHeight < height) {
        columns.add(column);
        column = new TextColumn();
        columnHeight = remainingHeight;
      }

      columnHeight = columnHeight - height;
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

    public int getWidth() {
      int width = 0;
      for (TextBlock block : blocks) {
        if (block.getWidth() > width) {
          width = block.getWidth();
        }
      }
      return width;
    }


    public void renderAt(double x, double y) {
      double startY = y;
      for (TextBlock block : blocks) {
        startY = block.renderAt(x, startY);
      }
    }

    public double getHeight() {
      double height = 0;
      for (TextBlock block : blocks) {
        height = height + block.getHeight();
      }
      return height;
    }
  }

  class TextBlock {
    private final List<String> lines;
    private final List<Boolean> externals;
    private final GraphicsContext g;
    private final CardTemplate template;

    TextBlock(GraphicsContext g, CardTemplate template) {
      this.lines = new ArrayList<>();
      this.externals = new ArrayList<>();
      this.g = g;
      this.template = template;
    }

    public void addLine(String line, boolean isExternal) {
      this.lines.add(line);
      this.externals.add(isExternal);
    }

    public double renderAt(double x, double y) {
      double fontSize = g.getFont().getSize();

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
        g.fillText(line, x, y + fontSize * 0.45);
        y = y + fontSize + template.getRowMargin();
      }
      return y;
    }

    public double getHeight() {
      // +1 to render an extra blank line with space distributed above and below
      return (this.lines.size() + 1) * (g.getFont().getSize() + template.getRowMargin()); 
    }

    public int getWidth() {
      int maxWidth = 0;
      for (String line : lines) {
        maxWidth = Math.max(maxWidth, getTextWidth(line + "   ", g.getFont()));
      }
      return maxWidth;
    }

    public boolean isEmpty() {
      return lines.isEmpty();
    }
  }
}
