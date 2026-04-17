package org.hotiver.service.product;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.ProductImage;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.dto.product.ProductImageDto;
import org.hotiver.dto.product.SellerInventoryProductDto;
import org.hotiver.repo.ProductImageRepo;
import org.hotiver.repo.ProductRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.storage.ImageStorageService;
import org.hotiver.service.storage.MinioImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductImageService {

    private final ImageStorageService imageStorageService;
    private final CurrentUserService currentUserService;
    private final ProductRepo productRepo;
    private final ProductImageRepo productImageRepo;

    @Value("${storage.max-images-count.product}")
    private Integer maxProductImageCount;
    @Value("${storage.host}")
    private String storageHost;
    @Value("${minio.bucket}")
    private String storageBucket;

    public ProductImageService(MinioImageStorageService imageStorageService,
                               CurrentUserService currentUserService, ProductRepo productRepo,
                               ProductImageRepo productImageRepo) {
        this.imageStorageService = imageStorageService;
        this.currentUserService = currentUserService;
        this.productRepo = productRepo;
        this.productImageRepo = productImageRepo;
    }

    public void deleteProductImage(Long productId, Long imageId) {
        Product product = getProduct(productId);

        SecurityUser user = currentUserService.getUserPrincipal();
        validateOwnership(product, user.getId());

        List<ProductImage> productImages = getImagesOfProduct(product);

        ProductImage imageToRemove = findImage(productImages, imageId);

        reassignMainImageIfNeeded(productImages, imageToRemove);

        deleteFromStorage(productId, imageToRemove);

        deleteImagesFromProductEntity(product, imageToRemove);
    }

    private void deleteImagesFromProductEntity(Product product, ProductImage imageToRemove) {
        product.getImages().remove(imageToRemove);
        productImageRepo.delete(imageToRemove);
        productRepo.save(product);
    }

    public void makeProductMainImage(Long productId, Long imageId) {
        Product product = getProduct(productId);
        SecurityUser user = currentUserService.getUserPrincipal();
        validateOwnership(product, user.getId());

        List<ProductImage> productImages = getImagesOfProduct(product);

        productImages.forEach(img -> img.setIsMain(false));

        for (var image : productImages) {
            if (image.getId().equals(imageId)) {
                image.setIsMain(true);
                break;
            }
        }

        product.setImages(productImages);
        productRepo.save(product);
    }

    public void addImageToProduct(Product product, MultipartFile image) {
        if (product.getImages().size() >= maxProductImageCount) {
            throw new RuntimeException("Maximum image count reached");
        }
        SecurityUser user = currentUserService.getUserPrincipal();
        validateOwnership(product, user.getId());
        if (image != null && !image.isEmpty()) {
            String imageStorePath;

            imageStorePath = imageStorageService.saveImage("products",
                    product.getId(),
                    image);

            boolean isMainImage = product.getImages().isEmpty();

            ProductImage productImage = new ProductImage(product, imageStorePath, isMainImage);

            product.addProductImage(productImage);
            productRepo.save(product);
        }
    }

    public void deleteAllImages(Long productId) {
        imageStorageService.deleteAllImages("products", productId);
    }

    public List<ProductImageDto> getProductImagesDto(Product product) {
        List<ProductImageDto> images = new ArrayList<>();

        for (var image : product.getImages()) {
            images.add(new ProductImageDto(
                    image.getId(),
                    storageHost + "/images" + image.getUrl(),
                    image.getIsMain()
            ));
        }
        return images;
    }

    private List<ProductImage> getImagesOfProduct(Product product) {
        SecurityUser user = currentUserService.getUserPrincipal();

        validateOwnership(product, user.getId());

        return product.getImages();
    }

    private void validateOwnership(Product product, Long userId) {
        if (!product.getSeller().getId().equals(userId)) {
            throw new AccessDeniedException("You are not the seller of this product");
        }
    }

    public void setSellerInventoryProductDtoImageUrl(
            List<SellerInventoryProductDto> sellerProducts) {

        sellerProducts.forEach(sellerInventoryProduct -> {
           sellerInventoryProduct
                   .setMainImageUrl(storageHost + "/"
                           + storageBucket
                           + sellerInventoryProduct.getMainImageUrl());
        });

    }

    public void addHostToImage(List<ListProductDto> products) {
        products.forEach(this::addHost);
    }

    public void addHostToImage(Page<ListProductDto> products) {
        products.forEach(this::addHost);
    }

    private void addHost(ListProductDto product) {
        product.setMainImageUrl(storageHost + "/images" + product.getMainImageUrl());
    }

    private Product getProduct(Long productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product with id "
                        + productId + " not found"));
    }

    private ProductImage findImage(List<ProductImage> productImages, Long imageId) {
        return productImages
                .stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));
    }

    private void reassignMainImageIfNeeded(List<ProductImage> images, ProductImage removed) {
        if (!removed.getIsMain()) return;

        images.stream()
                .filter(img -> !img.getId().equals(removed.getId()))
                .findFirst()
                .ifPresent(img -> img.setIsMain(true));
    }

    private void deleteFromStorage(Long productId, ProductImage imageToRemove) {
        imageStorageService.deleteImage("products",
                productId,
                imageToRemove.getUrl());
    }
}
