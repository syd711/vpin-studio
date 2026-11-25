package de.mephisto.vpin.server.textedit;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * Text of a file together with the encoding it was stored in, so that files which are not UTF-8
 * (e.g. a VPinballX.ini written in the Windows ANSI codepage) can be read and written back unchanged.
 */
public record TextFileContent(String text, Charset charset, boolean bom) {

  private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
  private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

  /**
   * Reads the file as UTF-8, falling back to windows-1252 and finally ISO-8859-1 (which never fails).
   * A UTF-8 BOM is stripped from the text and remembered.
   */
  public static TextFileContent read(File file) throws IOException {
    byte[] raw = Files.readAllBytes(file.toPath());
    boolean bom = raw.length >= 3 && raw[0] == UTF8_BOM[0] && raw[1] == UTF8_BOM[1] && raw[2] == UTF8_BOM[2];
    byte[] body = bom ? Arrays.copyOfRange(raw, 3, raw.length) : raw;
    for (Charset cs : List.of(StandardCharsets.UTF_8, WINDOWS_1252)) {
      try {
        CharsetDecoder decoder = cs.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
        return new TextFileContent(decoder.decode(ByteBuffer.wrap(body)).toString(), cs, bom);
      }
      catch (CharacterCodingException e) {
        // try next charset
      }
    }
    return new TextFileContent(new String(body, StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1, false);
  }

  /**
   * Writes the text using the encoding the existing file has. If the file does not exist yet or the
   * new text contains characters that encoding cannot represent, UTF-8 is used.
   */
  public static void write(File file, String text) throws IOException {
    Charset charset = StandardCharsets.UTF_8;
    boolean bom = false;
    if (file.exists()) {
      TextFileContent existing = read(file);
      bom = existing.bom();
      if (existing.charset().newEncoder().canEncode(text)) {
        charset = existing.charset();
      }
    }
    byte[] bytes = text.getBytes(charset);
    if (bom && StandardCharsets.UTF_8.equals(charset)) {
      byte[] withBom = new byte[UTF8_BOM.length + bytes.length];
      System.arraycopy(UTF8_BOM, 0, withBom, 0, UTF8_BOM.length);
      System.arraycopy(bytes, 0, withBom, UTF8_BOM.length, bytes.length);
      bytes = withBom;
    }
    Files.write(file.toPath(), bytes);
  }
}
