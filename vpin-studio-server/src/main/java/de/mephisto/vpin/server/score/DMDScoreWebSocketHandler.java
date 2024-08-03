package de.mephisto.vpin.server.score;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
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

  private int width = -1;
  private int height = -1;
   
  private int[] palette;

  List<ByteBuffer> buffers = new ArrayList<>();
  boolean partialMessage = false;

  private GameToProcessorFactory factory = new GameToProcessorFactory();

  private DMDScoreProcessor processor;

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
    try {
      if(message.isLast() == false) {     
        LOG.info("Partial Message Received");
        partialMessage = true;
        buffers.add(message.getPayload());
        return;
      }
      //else
      ByteBuffer frameData = message.getPayload();

      if (partialMessage) {
        buffers.add(frameData);
        frameData = concat(buffers);
        buffers.clear();
        partialMessage = false;
      }

      // now process the message
      frameData.order(ByteOrder.LITTLE_ENDIAN);
      String typeString = stringFromData(frameData, true);

      //LOG.info("New Binary Message Received " + typeString);
    
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
          int timeStamp = frameData.getInt();
          byte[] rgb24 = planesFromRgb24(frameData, width, height);
          Frame frame = new Frame(type, "", timeStamp, rgb24, width, height);
          processor.onFrameReceived(frame, palette);
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
    catch (Exception e) {
      LOG.info("Error while processing message (message ignored), error was : " + e.getMessage());
    }
  }

  private void processGameStart(String newGameName) {
    if (!StringUtils.equals(newGameName, gameName)) {
      // new game started, close previous one
      processGameStop();

      this.gameName = newGameName;
      processor = factory.getProcessor(gameName);
      if (processor != null) {
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

    if (processor != null) {
      byte[] frameBytes = DmdImageUtils.toPlane(planes, nbPlanes, width, height);
      Frame frame = new Frame(type, "", timeStamp, frameBytes, width, height);
      processor.onFrameReceived(frame, palette);
    }
  }

  private void processGameStop() {
    if (gameName != null) {
      if (processor != null) {
        processor.onFrameStop(gameName);
      }
      this.gameName = null;
    }
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    LOG.info("connection established");
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    LOG.info("connection closed");
    processGameStop();
  }

  @Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    LOG.error("transport error", exception);
  }
  
  @Override
	public boolean supportsPartialMessages() {
		return true;
	}

  private ByteBuffer concat(List<ByteBuffer> buffers) {
    final ByteBuffer combined = ByteBuffer.allocate(buffers.stream().mapToInt(ByteBuffer::remaining).sum());
    buffers.stream().forEach(b -> combined.put(b));
    return combined.rewind();
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

  private byte[] planesFromRgb24(ByteBuffer data, int width, int height) {
    byte[] colors = new byte[data.remaining()];
    data.get(colors);

    List<Integer> palette = new ArrayList<>();

    if (colors.length == width * height * 3) {
      final byte[] plane = new byte[width * height];
      for (int y = 0; y < height; y++) {
        int yWidth = y * width;
        for (int x = 0; x < width; x++) {
            final int index = (yWidth + x) * 3;
            // RGB24 is in BGR order
            final int r = (0xFF & colors[index]);
            final int g = (0xFF & colors[index + 1]);
            final int b = (0xFF & colors[index + 2]);
            int rgb = DmdImageUtils.rgb(r, g, b);
            int position = palette.indexOf(rgb);
            if (position < 0) {
              position = palette.size();
              palette.add(rgb);
            }
            plane[y * width + x] = (byte) position;
        }
      }
      // set the discovered palette
      this.palette = new int[palette.size()];
      Arrays.setAll(this.palette,  i -> palette.get(i));
      // and return the plane
      return plane;
    }
    else {
      LOG.error("Dimension does not match");
      return null;
    }
  }


}