package de.mephisto.vpin.server.nvrams.parser;

/**
 * Utility methods for DIP switch operations.
 */
public class DipSwitchUtils {

    /**
     * Return state of a game's DIP switch.
     * @param dipSwitches byte array with DIP switches stored in last 6 bytes
     * @param index DIP switch number (1 to n)
     * @return true if DIP switch is ON
     */
    public static boolean dipswGet(byte[] dipSwitches, int index) {
        index -= 1; // switch numbering starts at 1 in map, 0 in memory
        int bank = index / 8;
        int mask = 1 << (index % 8);
        return ((dipSwitches[dipSwitches.length - 6 + bank] & 0xFF) & mask) != 0;
    }

    /**
     * Set the state of a game's DIP switch.
     * @param dipSwitches byte array with DIP switches stored in last 6 bytes
     * @param index DIP switch number (1 to n)
     * @param state true to set ON, false to set OFF
     */
    public static void dipswSet(byte[] dipSwitches, int index, boolean state) {
        index -= 1;
        int bank = index / 8;
        int mask = 1 << (index % 8);
        int pos = dipSwitches.length - 6 + bank;
        if (state) {
            dipSwitches[pos] |= (byte) mask;
        } else {
            dipSwitches[pos] &= (byte) ~mask;
        }
    }
}
