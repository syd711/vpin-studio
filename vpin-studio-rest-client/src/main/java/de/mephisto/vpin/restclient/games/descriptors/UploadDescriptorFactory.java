package de.mephisto.vpin.restclient.games.descriptors;

import org.springframework.web.multipart.MultipartFile;

public class UploadDescriptorFactory {

  public static UploadDescriptor error(String message) {
    UploadDescriptor descriptor = new UploadDescriptor();
    descriptor.setError(message);
    return descriptor;
  }

  public static UploadDescriptor create(MultipartFile file, int gameId) {
    UploadDescriptor descriptor = new UploadDescriptor();
    descriptor.setGameId(gameId);
    descriptor.setFile(file);
    descriptor.setOriginalUploadedFileName(file.getOriginalFilename());
    return descriptor;
  }

  public static UploadDescriptor create(MultipartFile file) {
    return create(file, 0);
  }
}
