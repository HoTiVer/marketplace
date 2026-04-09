package org.hotiver.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageStorageService {

    public String saveImage(String entityType, Long entityId, MultipartFile file) throws IOException;

    public void deleteImage(String entityType, Long entityId, String fileUrl) throws IOException;

    public void deleteAllImages(String entityType, Long entityId) throws IOException;

}
