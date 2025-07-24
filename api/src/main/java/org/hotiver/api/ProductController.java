package org.hotiver.api;

import org.hotiver.dto.ProductAddDto;
import org.hotiver.dto.ProductGetDto;
import org.hotiver.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String mainPage(){
        return "main page";
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductGetDto> getProductById(@PathVariable Long id){
        return productService.getProductById(id);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/product")
    public ResponseEntity<?> addProduct(@RequestBody ProductAddDto productAddDto){
        return productService.addProduct(productAddDto);
    }

    @PreAuthorize("hasAnyRole('SELLER', 'MODERATOR','ADMIN')")
    @DeleteMapping("/product/{id}")
    public ResponseEntity<?> deleteProductById(@PathVariable Long id){
        return productService.deleteProductById(id);
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<?> updateProductById(@PathVariable Long id,
                                               @RequestBody ProductAddDto productAddDto){
        return productService.updateProductById(id, productAddDto);
    }

}
