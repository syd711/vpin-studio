package de.mephisto.vpin.server.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class XMLUtil {

  public static String prettyPrintXsl = 
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\r\n" +
        "    <xsl:strip-space elements=\"*\"/>\r\n" +
        "    <xsl:output method=\"xml\" encoding=\"UTF-8\"/>\r\n" +
        "\r\n" +
        "    <xsl:template match=\"@*|node()\">\r\n" +
        "        <xsl:copy>\r\n" +
        "            <xsl:apply-templates select=\"@*|node()\"/>\r\n" +
        "        </xsl:copy>\r\n" +
        "    </xsl:template>\r\n" +
        "\r\n" +
        "</xsl:stylesheet>";

  /**
   * Writes the document into a temporary file next to the target and then moves it over the target,
   * so readers never see a partially written file.
   */
  public static void write(File xmlFile, Document doc, boolean ignoreDeclaration) throws IOException, TransformerException {
    File tmpFile = new File(xmlFile.getAbsoluteFile().getParentFile(), xmlFile.getName() + ".tmp");
    try {
      writeDirect(tmpFile, doc, ignoreDeclaration);
      try {
        Files.move(tmpFile.toPath(), xmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      }
      catch (AtomicMoveNotSupportedException e) {
        Files.move(tmpFile.toPath(), xmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
    }
    finally {
      Files.deleteIfExists(tmpFile.toPath());
    }
  }

  private static void writeDirect(File xmlFile, Document doc, boolean ignoreDeclaration) throws IOException, TransformerException {
    try (Writer writer = Files.newBufferedWriter(xmlFile.toPath(), StandardCharsets.UTF_8)) {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute("indent-number", 2);

      Transformer transformer = prettyPrintXsl == null ?
          transformerFactory.newTransformer() :
          transformerFactory.newTransformer(new StreamSource(new StringReader(prettyPrintXsl)));
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, ignoreDeclaration ? "yes" : "no");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(writer);
      transformer.transform(source, result);
    }
  }
}
