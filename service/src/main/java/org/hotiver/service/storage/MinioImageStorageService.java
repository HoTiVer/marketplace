package org.hotiver.service.storage;

import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MinioImageStorageService implements ImageStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public MinioImageStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }


    @Override
    public String saveImage(String entityType, Long entityId, MultipartFile file) throws IOException {
        String fileName = "/" + entityType + "/" + entityId + "/" + file.getOriginalFilename();

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Failed to upload file", e);
        }

        return fileName;
    }

    @Override
    public void deleteImage(String entityType, Long entityId, String fileUrl) throws IOException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileUrl)
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Failed to delete file", e);
        }
    }

    @Override
    public void deleteAllImages(String entityType, Long entityId) throws IOException {
        String prefix = entityType + "/" + entityId + "/";

        try {
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : objects) {
                Item item = result.get();

                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(item.objectName())
                                .build()
                );
            }

        } catch (Exception e) {
            throw new IOException("Failed to delete all images for " + entityType + " " + entityId, e);
        }
    }
}
