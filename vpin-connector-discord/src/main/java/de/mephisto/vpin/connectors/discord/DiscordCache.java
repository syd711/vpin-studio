package de.mephisto.vpin.connectors.discord;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordCache {

  private final static Map<Long, List<DiscordMember>> membersByServer = new ConcurrentHashMap<>();

  public static List<DiscordMember> getMembers(long serverId) {
    if(membersByServer.containsKey(serverId)) {
      return membersByServer.get(serverId);
    }
    return null;
  }

  public static void put(long serverId, List<DiscordMember> memberList) {
    membersByServer.put(serverId, memberList);
  }

  public static void invalidate(long serverId) {
    membersByServer.remove(serverId);
  }

  public static void invalidateAll() {
    membersByServer.clear();
  }

  public static boolean contains(long serverId) {
    return membersByServer.containsKey(serverId);
  }
}
