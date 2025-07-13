package org.hotiver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotiver.domain.Entity.Product;
import org.hotiver.dto.ProductAddDto;
import org.hotiver.dto.ProductGetDto;
import org.hotiver.repo.ProductRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepo productRepo;
    private final ObjectMapper mapper;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
        mapper = new ObjectMapper();
    }

    public String addProduct(ProductAddDto productAddDto) {

        Product product = Product.builder()
                .name(productAddDto.getName())
                .price(productAddDto.getPrice())
                .category(productAddDto.getCategory())
                .description(productAddDto.getDescription())
                .build();

        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(productAddDto.getCharacteristic());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        product.setCharacteristic(jsonString);

        productRepo.save(product);

        return "something";
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
