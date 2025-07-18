package org.hotiver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.ProductAddDto;
import org.hotiver.dto.ProductGetDto;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    private final SellerRepo sellerRepo;
    private final ProductRepo productRepo;
    private final ObjectMapper mapper;

    public ProductService(SellerRepo sellerRepo, ProductRepo productRepo) {
        this.sellerRepo = sellerRepo;
        this.productRepo = productRepo;
        mapper = new ObjectMapper();
    }

    public ResponseEntity<Map<String, Object>> addProduct(ProductAddDto productAddDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Seller seller = sellerRepo.findByEmail(email);


        Product product = Product.builder()
                .name(productAddDto.getName())
                .price(productAddDto.getPrice())
                .category(productAddDto.getCategory())
                .description(productAddDto.getDescription())
                .seller(seller)
                .build();

        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(productAddDto.getCharacteristic());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        product.setCharacteristic(jsonString);

        productRepo.save(product);

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
                .category(existingProduct.getCategory())
                .description(existingProduct.getDescription())
                .sellerUsername(existingProduct.getSeller().getNickname())
                .sellerDisplayName(existingProduct.getSeller().getUser().getDisplayName())
                .build();

        try {
            var map = mapper.readValue(existingProduct.getCharacteristic(), Map.class);
            returnProduct.setCharacteristic(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok().body(returnProduct);
    }

    public void deleteProductById(Long id) {

        productRepo.deleteById(id);
    }
}
