package org.hotiver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hotiver.domain.Entity.Category;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.product.ProductAddDto;
import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.repo.CategoryRepo;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ProductService {

    private final ChatService chatService;
    private final SellerRepo sellerRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final CategoryRepo categoryRepo;

    public ProductService(ChatService chatService, SellerRepo sellerRepo,
                          ProductRepo productRepo, UserRepo userRepo,
                          CategoryRepo categoryRepo) {
        this.chatService = chatService;
        this.sellerRepo = sellerRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
    }

    public ResponseEntity<Map<String, Object>> addProduct(ProductAddDto productAddDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Seller seller = sellerRepo.findByEmail(email);
        Optional<Category> category = categoryRepo.findByName(productAddDto.getCategoryName());
        if (category.isEmpty())
            return ResponseEntity.badRequest().build();


        Product product = Product.builder()
                .name(productAddDto.getName())
                .price(productAddDto.getPrice())
                .category(category.get())
                .description(productAddDto.getDescription())
                .characteristic(new HashMap<>(productAddDto.getCharacteristic()))
                .seller(seller)
                .isVisible(false)
                .build();

        try {
            productRepo.save(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().body(Map.of("message", "new product added"));
    }

    public ResponseEntity<ProductGetDto> getProductById(Long id) {
        Optional<Product> product = productRepo.findById(id);

        if (product.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        var existingProduct = product.get();

        ProductGetDto returnProduct = ProductGetDto.builder()
                .name(existingProduct.getName())
                .price(existingProduct.getPrice())
                .categoryName(existingProduct.getCategory().getName())
                .description(existingProduct.getDescription())
                .characteristic(existingProduct.getCharacteristic())
                .sellerUsername(existingProduct.getSeller().getNickname())
                .sellerDisplayName(existingProduct.getSeller().getUser().getDisplayName())
                .build();

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
                        "admin deleted your product");

                productRepo.deleteById(id);
                return ResponseEntity.ok().build();
            }
        }

        if (Objects.equals(product.getSeller().getId(), user.getId())){
            productRepo.deleteById(id);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<?> updateProductById(Long id, ProductAddDto productAddDto) {
        Optional<Product> opProduct = productRepo.findById(id);
        if (opProduct.isEmpty()){
            return ResponseEntity.notFound().build();
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

        String categoryName = productAddDto.getCategoryName();
        if (categoryName != null) {
            Optional<Category> category = categoryRepo.findByName(categoryName);

            if (category.isEmpty())
                return ResponseEntity.badRequest().build();


            product.setCategory(category.get());
        }

        if (productAddDto.getCharacteristic() != null) {
//            String jsonString = null;
//            try {
//                jsonString = mapper.writeValueAsString(productAddDto.getCharacteristic());
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
            //product.setCharacteristic(jsonString);
            product.setCharacteristic(productAddDto.getCharacteristic());
        }

        productRepo.save(product);
        return ResponseEntity.ok().build();
    }
}
