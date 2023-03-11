package de.mephisto.vpin.server.puppack;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

@Service
public class PupPackService {

  public static void main(String[] args) throws IOException {
    File folder = new File("C:\\vPinball\\PinUPSystem\\PUPVideos");
    File[] files = folder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });

    int count = 0;
    int withDefault = 0;
    for (File file : files) {
      PupDefaultVideoResolver pupDefaultVideoResolver = new PupDefaultVideoResolver(file);
      File defaultVideo = pupDefaultVideoResolver.findDefaultVideo();
      if(defaultVideo == null) {
        System.out.println("No video found for " + file.getAbsolutePath());
      }
      else {
        System.out.println(defaultVideo.getAbsolutePath());
      }
    }

  }
}
