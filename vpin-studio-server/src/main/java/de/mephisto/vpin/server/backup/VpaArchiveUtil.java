package de.mephisto.vpin.server.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.ArchivePackageInfo;
import de.mephisto.vpin.restclient.TableDetails;
import de.mephisto.vpin.server.util.vpreg.VPReg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class VpaArchiveUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VpaArchiveUtil.class);

  public static List<TableDetails> readTableDetails(String json) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      return Arrays.asList(objectMapper.readValue(json, TableDetails[].class));
    } catch (IOException e) {
      LOG.error("Failed to read manifest data from json\n" + json + ": " + e.getMessage(), e);
    }
    return null;
  }

  public static TableDetails readTableDetails(File file) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

      ZipFile zipFile = new ZipFile(file);
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.getName().equals(TableDetails.ARCHIVE_FILENAME)) {
          InputStream stream = zipFile.getInputStream(entry);
          String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
          stream.close();
          zipFile.close();

          return objectMapper.readValue(text, TableDetails.class);
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to read manifest information from " + file.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return null;
  }

  public static String readVPRegJson(File file) {
    try {
      ZipFile zipFile = new ZipFile(file);
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.getName().equals(VPReg.ARCHIVE_FILENAME)) {
          InputStream stream = zipFile.getInputStream(entry);
          String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
          stream.close();
          zipFile.close();

          return text;
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to read VPReg.stg information from " + file.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return null;
  }


  public static ArchivePackageInfo readPackageInfo(File file) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

      ZipFile zipFile = new ZipFile(file);
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.getName().equals(ArchivePackageInfo.ARCHIVE_FILENAME)) {
          InputStream stream = zipFile.getInputStream(entry);
          String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
          stream.close();
          zipFile.close();

          return objectMapper.readValue(text, ArchivePackageInfo.class);
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to read manifest information from " + file.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return null;
  }
}
