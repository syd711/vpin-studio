package de.mephisto.vpin.connectors.discord;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordMemberCache {

  private final static Map<Long, DiscordMember> memberById = new ConcurrentHashMap<>();

  public static DiscordMember getMember(long memberId) {
    if(memberById.containsKey(memberId)) {
      return memberById.get(memberId);
    }
    return null;
  }

  public static void put(long memberId, DiscordMember member) {
    memberById.put(memberId, member);
  }

  public static void invalidate(long memberId) {
    memberById.remove(memberId);
  }

  public static void invalidateAll() {
    memberById.clear();
  }

  public static boolean contains(long memberId) {
    return memberById.containsKey(memberId);
  }
}
