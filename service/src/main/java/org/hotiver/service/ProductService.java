package org.hotiver.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hotiver.domain.Entity.*;
import org.hotiver.dto.product.CurrentSellerProductDto;
import org.hotiver.dto.product.ProductAddDto;
import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.dto.product.ProductImageDto;
import org.hotiver.dto.seller.SellerProductProjection;
import org.hotiver.repo.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class ProductService {

    private final ChatService chatService;
    private final SellerRepo sellerRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final CategoryRepo categoryRepo;
    private final ImageService imageService;
    private final ProductImageRepo productImageRepo;

    public ProductService(ChatService chatService, SellerRepo sellerRepo,
                          ProductRepo productRepo, UserRepo userRepo,
                          CategoryRepo categoryRepo, ImageService imageService, ProductImageRepo productImageRepo) {
        this.chatService = chatService;
        this.sellerRepo = sellerRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.imageService = imageService;
        this.productImageRepo = productImageRepo;
    }

    public ResponseEntity<Map<String, Object>> addProduct(ProductAddDto productAddDto,
                                                          MultipartFile image) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Seller seller = sellerRepo.findByEmail(email).orElseThrow();
        Category category = categoryRepo.findByName(productAddDto.getCategoryName())
                .orElseThrow();

        Product product = Product.builder()
                .name(productAddDto.getName())
                .price(productAddDto.getPrice())
                .category(category)
                .description(productAddDto.getDescription())
                .characteristic(new HashMap<>(productAddDto.getCharacteristic()))
                .seller(seller)
                .stockQuantity(productAddDto.getQuantity())
                .salesCount(0)
                .publishingDate(Date.valueOf(LocalDate.now()))
                .rating(BigDecimal.valueOf(0.0))
                .isVisible(true)
                .build();

        product = productRepo.save(product);

        try {
            if (image != null && !image.isEmpty()) {
                String imageUrl = imageService.saveProductImage(product.getId(), image);

                ProductImage productImage = ProductImage.builder()
                        .product(product)
                        .url(imageUrl)
                        .isMain(true)
                        .build();

                product.addProductImage(productImage);

                productRepo.save(product);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Product created but image upload failed"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Product created",
                "productId", product.getId()
        ));
    }


    public ResponseEntity<ProductGetDto> getProductById(Long id) {
        Optional<Product> product = productRepo.findById(id);

        if (product.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        if (!product.get().getIsVisible())
            return ResponseEntity.notFound().build();

        Product existingProduct = product.get();

        ProductGetDto returnProduct = ProductGetDto.builder()
                .id(existingProduct.getId())
                .name(existingProduct.getName())
                .price(existingProduct.getPrice())
                .categoryName(existingProduct.getCategory().getName())
                .description(existingProduct.getDescription())
                .characteristic(existingProduct.getCharacteristic())
                .sellerUsername(existingProduct.getSeller().getNickname())
                .sellerDisplayName(existingProduct.getSeller().getUser().getDisplayName())
                .build();

        List<ProductImageDto> images = new ArrayList<>();

        for (var image : existingProduct.getImages()) {
            images.add(new ProductImageDto(
                image.getId(),
                    "/images" + image.getUrl(),
                    image.getIsMain()
            ));
        }

        returnProduct.setImages(images);

        return ResponseEntity.ok().body(returnProduct);
    }

    @Transactional
    public ResponseEntity<?> deleteProductById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var roles = authentication.getAuthorities();

        User user = userRepo.findByEmail(email).get();

        Optional<Product> opProduct = productRepo.findById(id);
        if (opProduct.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        var product = opProduct.get();

        for (var role : roles){
            if (role.getAuthority().equals("ROLE_ADMIN")){
                chatService.sendMessage(0L, user.getId(),
                        "admin deleted your product with id: " + product.getId());

                productRepo.deleteById(id);
                try {
                    imageService.deleteAllProductImages(id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return ResponseEntity.ok().build();
            }
        }

        if (Objects.equals(product.getSeller().getId(), user.getId())){
            productRepo.deleteById(id);
            try {
                imageService.deleteAllProductImages(id);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<?> updateProductById(Long id,
                                               ProductAddDto productAddDto,
                                               MultipartFile image) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Product> opProduct = productRepo.findById(id);
        if (opProduct.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        User user = userRepo.findByEmail(email).get();

        if (!opProduct.get().getSeller().getId().equals(user.getId())){
            return ResponseEntity.status(403).build();
        }

        var product = opProduct.get();

        if (productAddDto.getName() != null) {
            product.setName(productAddDto.getName());
        }

        if (productAddDto.getPrice() != null) {
            product.setPrice(productAddDto.getPrice());
        }

        if (productAddDto.getDescription() != null) {
            product.setDescription(productAddDto.getDescription());
        }

        if (productAddDto.getQuantity() != null) {
            product.setStockQuantity(productAddDto.getQuantity());
        }

        String categoryName = productAddDto.getCategoryName();
        if (categoryName != null) {
            Optional<Category> category = categoryRepo.findByName(categoryName);

            if (category.isEmpty())
                return ResponseEntity.badRequest().build();


            product.setCategory(category.get());
        }

        if (productAddDto.getCharacteristic() != null) {
            product.setCharacteristic(productAddDto.getCharacteristic());
        }

        productRepo.save(product);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<List<SellerProductProjection>> getCurrentSellerProducts(String username) {
        if (username == null)
            return  ResponseEntity.badRequest().build();

        Seller seller = sellerRepo.findByEmail(username).get();

        List<SellerProductProjection> productGetDto = productRepo
                .getCurrentSellerProducts(seller.getId());

        return ResponseEntity.ok().body(productGetDto);
    }

    public ResponseEntity<CurrentSellerProductDto> getCurrentSellerProductById(
            Long productId) {

        Optional<Product> product = productRepo.findById(productId);

        if (product.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        if (!product.get().getIsVisible())
            return ResponseEntity.notFound().build();

        Product existingProduct = product.get();

        CurrentSellerProductDto returnProduct = CurrentSellerProductDto.builder()
                .id(existingProduct.getId())
                .name(existingProduct.getName())
                .price(existingProduct.getPrice())
                .categoryName(existingProduct.getCategory().getName())
                .description(existingProduct.getDescription())
                .characteristic(existingProduct.getCharacteristic())
                .quantity(existingProduct.getStockQuantity())
                .build();

        List<ProductImageDto> images = new ArrayList<>();

        for (var image : existingProduct.getImages()) {
            images.add(new ProductImageDto(
                    image.getId(),
                    "/images" + image.getUrl(),
                    image.getIsMain()
            ));
        }
        returnProduct.setImages(images);

        return ResponseEntity.ok().body(returnProduct);
    }

    public ResponseEntity<?> deleteProductImage(Long productId, Long imageId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Product product = productRepo.findById(productId).orElse(null);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        var user = userRepo.findByEmail(email).orElse(null);
        if (!product.getSeller().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        var productImages = product.getImages();

        var imageToRemove = productImages
                .stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElse(null);

        if (imageToRemove == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            imageService.deleteProductImage(imageToRemove.getUrl());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        product.getImages().remove(imageToRemove);
        productImageRepo.delete(imageToRemove);

        productRepo.save(product);

        return ResponseEntity.ok().build();
    }


    public ResponseEntity<?> makeProductMainImage(Long productId, Long imageId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Product product = productRepo.findById(productId).orElse(null);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        var user = userRepo.findByEmail(email).orElse(null);
        if (!product.getSeller().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        var productImages = product.getImages();

        for (var image : productImages) {
            if (image.getIsMain()) {
                image.setIsMain(false);
                break;
            }
        }

        for (var image : productImages) {
            if (image.getId().equals(imageId)) {
                image.setIsMain(true);
                break;
            }
        }

        product.setImages(productImages);
        productRepo.save(product);
        return ResponseEntity.ok().build();
    }
}
