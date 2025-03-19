package de.mephisto.vpin.server.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

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

  public static void write(File xmlFile, Document doc, boolean ignoreDeclaration) throws IOException, TransformerException {
    try (FileWriter writer = new FileWriter(xmlFile)) {
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
