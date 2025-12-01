package de.mephisto.vpin.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IADownloader {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final static File ROM_FOLDER = new File("C:\\MAME\\roms\\");

  public static void main(String args[]) throws Exception {
//    String html = Jsoup.connect("https://archive.org/download/mame-chds-roms-extras-complete/MAME%200.256%20ROMs%20%28merged%29/").get().html();

    String html = FileUtils.readFileToString(new File("C:\\workspace\\vpin-studio-dev\\vpin-tools\\src\\main\\resources\\de.mephisto.vpin.tools\\test.txt"));
    Pattern p = Pattern.compile("href=\"(.*?)\"");
    Matcher m = p.matcher(html);
    String url = null;
    while (m.find()) {
      url = m.group(1); // this variable should contain the link URL
      System.out.println(url);

      if (!url.endsWith(".zip")) {
        continue;
      }
      File rom = new File(ROM_FOLDER, url);
      if(rom.exists()) {
        continue;
      }

      download(rom, "https://archive.org/download/mame-chds-roms-extras-complete/MAME%200.256%20ROMs%20%28merged%29/" + url);
    }

//    Document doc = Jsoup.parse(new URL("https://archive.org/download/mame-chds-roms-extras-complete/MAME%200.256%20ROMs%20%28merged%29/"), 15000);
//    Elements links = doc.select("a[href]"); // a with href
//    int index = 0;
//    System.out.println("Eval of " + links.size());
//    for (Element link : links) {
//index++;
//String target = link.attr("href");
//System.out.println("Found target: "  + index + ": " + target);
//if(!target.endsWith(".zip")) {
//  continue;
//}

//File rom = new File(ROM_FOLDER, target);
//if(rom.exists()) {
//  continue;
//}
//
//
//    }
  }

  private static void download(File rom, String downloadUrl) {
    try {
      if (rom.exists()) {
        rom.delete();
      }

      File tmp = new File(rom.getParentFile(), rom.getName() + ".bak");
      if (tmp.exists()) {
        tmp.delete();
      }

      LOG.info("Downloading " + downloadUrl);
      URL url = new URL(downloadUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setReadTimeout(5000);
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      FileOutputStream fileOutputStream = new FileOutputStream(tmp);
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      in.close();
      fileOutputStream.close();

      tmp.renameTo(rom);
      LOG.info("Downloaded file " + rom.getAbsolutePath());
    }
    catch (Exception e) {
      LOG.error("Failed to execute download: " + e.getMessage(), e);
    }
  }
}
