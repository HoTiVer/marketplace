package org.hotiver.service.storage;

import org.hotiver.common.Exception.storage.FileStorageException;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    public String saveImage(String entityType, Long entityId, MultipartFile file) throws FileStorageException;

    public void deleteImage(String entityType, Long entityId, String fileUrl) throws FileStorageException;

    public void deleteAllImages(String entityType, Long entityId) throws FileStorageException;

}
