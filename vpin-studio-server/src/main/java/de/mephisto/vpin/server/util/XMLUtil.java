package de.mephisto.vpin.server.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.util.StreamUtils;
import org.w3c.dom.Document;

public class XMLUtil {

  public static String prettyPrintXsl;

  static {
    try (InputStream in = XMLUtil.class.getResourceAsStream("prettyprint.xsl")) {
      prettyPrintXsl = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
    }
    catch (IOException ioe) {
      prettyPrintXsl = null;
    }
  }

  public static void write(File xmlFile, Document doc, boolean ignoreDeclaration) throws IOException, TransformerException {
    try (FileWriter writer = new FileWriter(xmlFile)) {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute("indent-number", 2);

      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, ignoreDeclaration ? "yes" : "no");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(writer);
      transformer.transform(source, result);
    }
  }
}
