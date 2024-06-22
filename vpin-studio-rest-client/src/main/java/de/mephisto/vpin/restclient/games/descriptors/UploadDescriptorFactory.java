package de.mephisto.vpin.restclient.games.descriptors;

import org.springframework.web.multipart.MultipartFile;

public class UploadDescriptorFactory {

  public static UploadDescriptor create() {
    UploadDescriptor uploadDescriptor = new UploadDescriptor();
    return uploadDescriptor;
  }

  public static UploadDescriptor error(String message) {
    UploadDescriptor descriptor = create();
    descriptor.setError(message);
    return descriptor;
  }

  public static UploadDescriptor create(MultipartFile file, int gameId) {
    UploadDescriptor descriptor = create();
    descriptor.setGameId(gameId);
    descriptor.setFile(file);
    descriptor.setOriginalUploadFileName(file.getOriginalFilename());
    return descriptor;
  }

  public static UploadDescriptor create(MultipartFile file) {
    return create(file, 0);
  }
}
