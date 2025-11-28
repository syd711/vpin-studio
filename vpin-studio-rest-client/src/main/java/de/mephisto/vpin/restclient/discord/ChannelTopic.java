package de.mephisto.vpin.restclient.discord;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Base64;
import java.util.Date;

public class ChannelTopic {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final static String TITLE = "VPin Studio Competition Channel";
  private long timestamp = new Date().getTime();
  private long messageId = 0;

  public long getMessageId() {
    return messageId;
  }

  public void setMessageId(long messageId) {
    this.messageId = messageId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String toTopic() {
    String data = timestamp + "," + messageId;

    StringBuilder builder = new StringBuilder(TITLE);
    builder.append(" (");
    builder.append(Base64.getEncoder().encodeToString(data.getBytes()));
    builder.append(")");
    return builder.toString();
  }

  public static ChannelTopic toChannelTopic(String topic) {
    try {
      if (!StringUtils.isEmpty(topic) && topic.contains("(") && topic.contains(")")) {
        String data = topic.substring(topic.indexOf('(') + 1, topic.lastIndexOf(')'));

        ChannelTopic channelTopic = new ChannelTopic();
        String dataString = new String(Base64.getDecoder().decode(data.getBytes()));
        long timeStamp = Long.parseLong(dataString.split(",")[0]);
        long messageId = Long.parseLong(dataString.split(",")[1]);
        channelTopic.setTimestamp(timeStamp);
        channelTopic.setMessageId(messageId);
        return channelTopic;
      }
    } catch (Exception e) {
      LOG.info("Failed to read channel topic data: " + e.getMessage(), e);
    }
    return null;
  }

  public boolean isOlderThanToday() {
    return new Date().getTime() - timestamp > (1000 * 60 * 60 * 24);
  }
}
