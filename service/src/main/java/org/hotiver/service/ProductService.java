package org.hotiver.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hotiver.domain.Entity.*;
import org.hotiver.dto.product.CurrentSellerProductDto;
import org.hotiver.dto.product.ProductAddDto;
import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.dto.product.ProductImageDto;
import org.hotiver.dto.seller.SellerProductProjection;
import org.hotiver.repo.*;
import org.springframework.security.access.AccessDeniedException;
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

    public void addProduct(ProductAddDto productAddDto,
                           MultipartFile image) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Seller seller = sellerRepo.findByEmail(email).orElseThrow();
        Category category = categoryRepo.findByName(productAddDto.getCategoryName())
                .orElseThrow(()-> new EntityNotFoundException("Category not found"));

        Product product = Product.builder()
                .name(productAddDto.getName())
                .price(productAddDto.getPrice())
                .category(category)
                .description(productAddDto.getDescription())
                .characteristic(new HashMap<>(productAddDto.getCharacteristics()))
                .seller(seller)
                .stockQuantity(productAddDto.getQuantity())
                .salesCount(0)
                .publishingDate(Date.valueOf(LocalDate.now()))
                .rating(BigDecimal.valueOf(0.0))
                .isVisible(true)
                .build();

        product = productRepo.save(product);

        if (image != null && !image.isEmpty()) {
            String imageUrl;
            try {
                imageUrl = imageService.saveProductImage(product.getId(), image);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ProductImage productImage = ProductImage.builder()
                    .product(product)
                    .url(imageUrl)
                    .isMain(true)
                    .build();

            product.addProductImage(productImage);

            productRepo.save(product);
        }
    }


    public ProductGetDto getProductById(Long id) {
        Optional<Product> product = productRepo.findById(id);

        if (product.isEmpty() || !product.get().getIsVisible()){
            throw new EntityNotFoundException("Product with id " + id + " not found");
        }

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

        return returnProduct;
    }

    @Transactional
    public void deleteProductById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var roles = authentication.getAuthorities();

        User user = userRepo.findByEmail(email).get();

        Optional<Product> opProduct = productRepo.findById(id);
        if (opProduct.isEmpty()){
            throw new EntityNotFoundException("Product with id " + id + " not found");
        }
        var product = opProduct.get();


        for (var role : roles) {
            if (role.getAuthority().equals("ROLE_ADMIN")){
                productRepo.deleteById(id);
                try {
                    imageService.deleteAllProductImages(id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                chatService.sendMessage(0L, user.getId(),
                        "admin deleted your product with id: " + product.getId());
            }
        }

        if (Objects.equals(product.getSeller().getId(), user.getId())){
            productRepo.deleteById(id);
            try {
                imageService.deleteAllProductImages(id);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void updateProductById(Long id,
                                  ProductAddDto productAddDto,
                                  MultipartFile image) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Product> opProduct = productRepo.findById(id);
        if (opProduct.isEmpty()){
            throw new EntityNotFoundException("Product with id " + id + " not found");
        }

        User user = userRepo.findByEmail(email).get();

        if (!opProduct.get().getSeller().getId().equals(user.getId())){
            throw new AccessDeniedException("You are not the seller of this product");
        }

        Product product = opProduct.get();

        String categoryName = productAddDto.getCategoryName();
        if (categoryName != null) {
            Optional<Category> category = categoryRepo.findByName(categoryName);

            if (category.isEmpty())
                throw new EntityNotFoundException("Category with name " + categoryName + " not found");

            product.setCategory(category.get());
        }


        if (image != null) {
            if (product.getImages().size() >= 10) {
                throw new RuntimeException();
            }

            String url;
            try {
                url = imageService.saveProductImage(product.getId(), image);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ProductImage productImage = ProductImage.builder()
                    .url(url)
                    .isMain(false)
                    .product(product)
                    .build();

            product.addProductImage(productImage);
        }

        productRepo.save(product);
    }

    public List<SellerProductProjection> getCurrentSellerProducts(String username) {
        if (username == null)
            throw new EntityNotFoundException("Seller do not exist");

        Seller seller = sellerRepo.findByEmail(username).get();

        return productRepo
                .getCurrentSellerProducts(seller.getId());
    }

    public CurrentSellerProductDto getCurrentSellerProductById(
            Long productId) {

        Optional<Product> product = productRepo.findById(productId);

        if (product.isEmpty() || !product.get().getIsVisible()){
            throw new EntityNotFoundException("Product with id " + productId + " not found");
        }

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

        return returnProduct;
    }

    public void deleteProductImage(Long productId, Long imageId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Product product = productRepo.findById(productId).orElse(null);

        if (product == null) {
            throw new EntityNotFoundException("Product with id " + productId + " not found");
        }

        var user = userRepo.findByEmail(email).orElse(null);
        if (!product.getSeller().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not the seller of this product");
        }

        var productImages = product.getImages();

        var imageToRemove = productImages
                .stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElse(null);

        try {
            imageService.deleteProductImage(imageToRemove.getUrl());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        product.getImages().remove(imageToRemove);
        productImageRepo.delete(imageToRemove);

        productRepo.save(product);
    }


    public void makeProductMainImage(Long productId, Long imageId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Product product = productRepo.findById(productId).orElse(null);

        if (product == null) {
            throw new EntityNotFoundException("Product with id " + productId + " not found");
        }

        var user = userRepo.findByEmail(email).orElse(null);
        if (!product.getSeller().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not the seller of this product");
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
    }
}
