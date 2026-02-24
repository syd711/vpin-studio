package de.mephisto.vpin.server.dmdscore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

public class DMDScoreWebSocketHandler extends AbstractWebSocketHandler {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreWebSocketHandler.class);

  private static final int DEFAULT_COLOR = 0xec843d;

  private String gameName;
  private int firstTimeStamp = -1;

  private int width = -1;
  private int height = -1;
   
  private int[] palette;

  private List<DMDScoreProcessor> processors = new ArrayList<>();


  public void addDMDScoreProcessor(DMDScoreProcessor processor) {
    processors.add(processor);
  }

  public void removeDMDScoreProcessor(DMDScoreProcessor processor) {
    processors.remove(processor);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
    LOG.info("New Text Message Received");
  }

  /**
   * Aligned with :
   * https://github.com/freezy/dmd-extensions/blob/master/LibDmd/Output/Network/WebsocketSerializer.cs
   */
  @Override
  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
    ByteBuffer frameData = message.getPayload().order(ByteOrder.LITTLE_ENDIAN);
    String typeString = stringFromData(frameData, true);

//    LOG.info("New Binary Message Received " + typeString);
  
    FrameType type = FrameType.getEnum(typeString);
  
    switch (type) {
      case GAME_NAME:
        processGameStart(stringFromData(frameData, false));
        break;
      case DIMENSIONS:
        width = frameData.getInt();
        height = frameData.getInt();
        LOG.info("Game dimension for {} :{} x {}", gameName, width, height);
        break;
      case COLORED_GRAY_2:
        processFrame(type, frameData.getInt(), paletteFromData(frameData), planesFromData(frameData), 2);
        break;
      case COLORED_GRAY_4:
        processFrame(type, frameData.getInt(), paletteFromData(frameData), planesFromData(frameData), 4);
        break;
      case COLORED_GRAY_6:
        processFrame(type, frameData.getInt(), paletteFromData(frameData), planesFromData(frameData), 6);
        break;
      case RGB24:
        //int[] rawImage = DmdImageUtils._toRawImageFromRgb24(planes, width, height);
        break;
      case GRAY_2_PLANES:
        processFrame(type, frameData.getInt(), palette, planesFromData(frameData), 2);
        break;
      case GRAY_4_PLANES:
        processFrame(type, frameData.getInt(), palette, planesFromData(frameData), 4);
        break;
      case COLOUR:
        int color = frameData.getInt();
        palette = DmdImageUtils.paletteFromColor(color, 4);
        LOG.info("Colour frame: {}", color);
        break;
      case CLEAR_COLOUR:
        palette = DmdImageUtils.paletteFromColor(DEFAULT_COLOR, 4);
        LOG.info("Clear colour frame");
        break;
      case PALETTE:
        palette = paletteFromData(frameData);
        LOG.info("Palette frame of length: {}", palette.length);
        break;
      case CLEAR_PALETTE:
        palette = null;
        LOG.info("Clear palette frame");
        break;
      case UNKNOWN:
        LOG.info("Message received with unknown type {}", typeString);
        break;
    }
  }

  private void processGameStart(String newGameName) {
    if (!StringUtils.equals(newGameName, gameName)) {
      // new game started, close previous one
      processGameStop();

      this.gameName = newGameName;

      for (DMDScoreProcessor processor : processors) {
        processor.onFrameStart(gameName);
      }

      LOG.info("Game name started : {}", gameName);
    }
  }

  private void processFrame(FrameType type, int timeStamp, int[] palette, byte[] planes, int nbPlanes) {
    if (width < 0 || height < 0) {
      LOG.warn("Don't try to process any frames that may come before we know the size of the display");
      return;
    }

    if (firstTimeStamp < 0) {
      firstTimeStamp = timeStamp;
    }

    byte[] frameBytes = DmdImageUtils.toPlane(planes, nbPlanes, width, height);
    if (frameBytes != null) {
      Frame frame = new Frame(type, timeStamp - firstTimeStamp, frameBytes, width, height, palette);
      for (DMDScoreProcessor processor : processors) {
        try {
          processor.onFrameReceived(frame);
        }
        catch (Exception e) {
          LOG.warn("Error while processing frame by {}: {}", processor.getClass().getName(), e.getMessage());
        }
      }
    }
  }

  private void processGameStop() {
    if (gameName != null) {

      for (DMDScoreProcessor processor : processors) {
        processor.onFrameStop(gameName);
      }

      this.gameName = null;
      this.firstTimeStamp = -1;
    }
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    LOG.info("connection established");
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    LOG.info("connection closed: {}", status.getReason());
    processGameStop();
  }

  //----------------------------------------

  private String stringFromData(final ByteBuffer data, final boolean skip) {
    if (data.hasRemaining()) {
      int start = data.position();
      byte b = data.get();
      if (skip) {
        // skip over any null bytes at the beginning of the data.
        while (b == 0) {
          b = data.get();
          start++;
        }
      }
      int length = 0;
      // Look through the data until we find a null (0) byte or we hit the end of the data
      while (b != 0) {
        length++;
        if (data.hasRemaining()) {
          b = data.get();
        } else {
          break;
        }
      }
      return new String(data.array(), start, length);
    }
    return "";
  }
  
  private int[] paletteFromData(final ByteBuffer data) {
    // First int is how many palette items to expect
    int[] palette = new int[data.getInt()];
    for (int i = 0; i < palette.length; i++) {
      palette[i] = data.getInt();
      //LOG.info("Palette[{}] = {} / {}", i, palette[i], DmdImageUtils.colorToHex(palette[i]));
    }
    return palette;
  }

  private byte[] planesFromData(final ByteBuffer data) {
    byte[] planes = new byte[data.remaining()];
    data.get(planes);
    return planes;
  }
}