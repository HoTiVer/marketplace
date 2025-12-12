package org.hotiver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${app.upload.dir}")
    private String rootDir;

    public String saveProductImage(Long productId, MultipartFile file) throws IOException {
        String folder = rootDir + "/products/" + productId;
        Files.createDirectories(Paths.get(folder));

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filepath = Paths.get(folder, filename);

        file.transferTo(filepath.toFile());

        return "/products/" + productId + "/" + filename;
    }

    public void deleteImage(String relativePath) throws IOException {
        Path file = Paths.get(rootDir + relativePath);
        Files.deleteIfExists(file);
    }

    public void deleteAllProductImages(Long productId) throws IOException {
        Path directory = Paths.get(rootDir + "/products/"+ productId.toString());
        if (Files.exists(directory)) {
            Files.walk(directory)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

}
