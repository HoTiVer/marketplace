package org.hotiver.service.product;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hotiver.common.Exception.seller.SellerNotFoundException;
import org.hotiver.domain.Entity.*;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.product.*;
import org.hotiver.repo.*;
import org.hotiver.service.chat.ChatService;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.mapper.ProductMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;

@Slf4j
@Service
public class ProductService {

    private final ChatService chatService;
    private final ProductImageService productImageService;
    private final CurrentUserService currentUserService;
    private final SellerRepo sellerRepo;
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ProductMapper productMapper;

    public ProductService(ChatService chatService, ProductImageService productImageService,
                          CurrentUserService currentUserService, SellerRepo sellerRepo,
                          ProductRepo productRepo, CategoryRepo categoryRepo,
                          ProductMapper productMapper) {
        this.chatService = chatService;
        this.productImageService = productImageService;
        this.currentUserService = currentUserService;
        this.sellerRepo = sellerRepo;
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.productMapper = productMapper;
    }

    @Transactional
    public void addProduct(ProductAddDto productAddDto,
                           MultipartFile image) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Seller seller = sellerRepo.findByEmail(email)
                .orElseThrow(() -> new SellerNotFoundException("You are not a seller"));
        Category category = categoryRepo.findByName(productAddDto.getCategoryName())
                .orElseThrow(()-> new EntityNotFoundException("Category not found"));

        Product product = productMapper.productAddDtoToEntity(
                productAddDto,
                category,
                seller);

        product = productRepo.save(product);

        productImageService.addImageToProduct(product, image);
        productRepo.save(product);
    }

    @Transactional
    public void deleteProductById(Long id) {
        SecurityUser user = currentUserService.getUserPrincipal();

        Product product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with id "
                        + id + " not found"));

        deleteProductIfAdmin(user, product);

        if (Objects.equals(product.getSeller().getId(), user.getId())){
            productRepo.deleteById(id);
            productImageService.deleteAllImages(product.getId());
        }
    }

    private void deleteProductIfAdmin(SecurityUser user, Product product) {
        for (var role : user.getAuthorities()) {
            if (role.getAuthority().equals("ROLE_ADMIN")) {
                productRepo.deleteById(product.getId());

                productImageService.deleteAllImages(product.getId());

                chatService.sendMessage(0L, user.getId(),
                        "Admin deleted your product: " + product.getName());
            }
        }
    }

    @Transactional
    public void updateProductById(Long id,
                                  ProductAddDto productAddDto,
                                  MultipartFile image) {
        SecurityUser user = currentUserService.getUserPrincipal();

        Product product = productRepo.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Product with id "
                        + id + " not found"));

        if (!product.getSeller().getId().equals(user.getId())){
            throw new AccessDeniedException("You are not the seller of this product");
        }

        setProductCategory(product, productAddDto.getCategoryName());
        productMapper.updateProductFromDto(productAddDto, product);

        productImageService.addImageToProduct(product, image);

        productRepo.save(product);
    }

    private void setProductCategory(Product product, String categoryName) {
        if (categoryName != null) {
            Optional<Category> category = categoryRepo.findByName(categoryName);

            if (category.isEmpty())
                throw new EntityNotFoundException("Category with name " +
                        categoryName + " not found");

            product.setCategory(category.get());
        }
    }
}
