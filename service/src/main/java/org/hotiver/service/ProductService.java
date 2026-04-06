package org.hotiver.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hotiver.common.Exception.auth.NoAuthorizationException;
import org.hotiver.domain.Entity.*;
import org.hotiver.dto.product.CurrentSellerProductDto;
import org.hotiver.dto.product.ProductAddDto;
import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.dto.product.ProductImageDto;
import org.hotiver.dto.seller.SellerProductProjection;
import org.hotiver.repo.*;
import org.hotiver.service.mapper.ProductMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final ProductMapper productMapper;

    public ProductService(ChatService chatService, SellerRepo sellerRepo,
                          ProductRepo productRepo, UserRepo userRepo,
                          CategoryRepo categoryRepo, ImageService imageService,
                          ProductImageRepo productImageRepo, ProductMapper productMapper) {
        this.chatService = chatService;
        this.sellerRepo = sellerRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.imageService = imageService;
        this.productImageRepo = productImageRepo;
        this.productMapper = productMapper;
    }

    public void addProduct(ProductAddDto productAddDto,
                           MultipartFile image) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Seller seller = sellerRepo.findByEmail(email).orElseThrow();
        Category category = categoryRepo.findByName(productAddDto.getCategoryName())
                .orElseThrow(()-> new EntityNotFoundException("Category not found"));

        Product product = productMapper.productAddDtoToEntity(productAddDto, category, seller);

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
        Product product = productRepo.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Product with id " +
                        id + " not found"));

        //List<ProductImageDto> images = getProductImages(product);
        //returnProduct.setImages(images);

        return productMapper.entityToProductGetDto(
                product,
                product.getCategory(),
                product.getSeller());
    }

    @Transactional
    public void deleteProductById(Long id) {
        User user = getCurrentUser();
        var roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        Product product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with id "
                        + id + " not found"));


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
        User user = getCurrentUser();

        Product product = productRepo.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Product with id "
                        + id + " not found"));

        if (!product.getSeller().getId().equals(user.getId())){
            throw new AccessDeniedException("You are not the seller of this product");
        }

        String categoryName = productAddDto.getCategoryName();
        if (categoryName != null) {
            Optional<Category> category = categoryRepo.findByName(categoryName);

            if (category.isEmpty())
                throw new EntityNotFoundException("Category with name " + categoryName + " not found");

            product.setCategory(category.get());
        }

        productMapper.updateProductFromDto(productAddDto, product);


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
        Seller seller = sellerRepo.findByEmail(username)
                .orElseThrow(()-> new EntityNotFoundException("Seller does not exist"));

        return productRepo
                .getCurrentSellerProducts(seller.getId());
    }

    public CurrentSellerProductDto getCurrentSellerProductById(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(()-> new EntityNotFoundException("Product with id "
                        + productId + " not found"));

        CurrentSellerProductDto returnProduct = CurrentSellerProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .categoryName(product.getCategory().getName())
                .description(product.getDescription())
                .characteristics(product.getCharacteristic())
                .quantity(product.getStockQuantity())
                .build();

        List<ProductImageDto> images = getProductImages(product);
        returnProduct.setImages(images);

        return returnProduct;
    }

    public void deleteProductImage(Long productId, Long imageId) {
        User user = getCurrentUser();

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product with id "
                        + productId + " not found"));


        if (!product.getSeller().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not the seller of this product");
        }

        List<ProductImage> productImages = product.getImages();

        ProductImage imageToRemove = productImages
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
        User user = getCurrentUser();

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product with id "
                        + productId + " not found"));

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

    private List<ProductImageDto> getProductImages(Product product) {
        List<ProductImageDto> images = new ArrayList<>();

        for (var image : product.getImages()) {
            images.add(new ProductImageDto(
                    image.getId(),
                    "/images" + image.getUrl(),
                    image.getIsMain()
            ));
        }
        return images;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepo.findByEmail(email)
                .orElseThrow(()-> new NoAuthorizationException("The user is not authorized"));
    }
}
