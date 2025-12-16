package org.hotiver.api;

import org.hotiver.dto.product.ProductAddDto;
import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.dto.seller.SellerProductProjection;
import org.hotiver.service.ImageService;
import org.hotiver.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductGetDto> getProductById(@PathVariable Long id){
        return productService.getProductById(id);
    }


    @PreAuthorize("hasRole('SELLER')")
    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(
            @RequestPart("data") ProductAddDto productAddDto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return productService.addProduct(productAddDto, image);
    }

    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @DeleteMapping("/product/{id}")
    public ResponseEntity<?> deleteProductById(@PathVariable Long id){
        return productService.deleteProductById(id);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping(value = "/product/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductById(
            @PathVariable Long id,
            @RequestPart("data") ProductAddDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return productService.updateProductById(id, dto, image);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PatchMapping("/product/{productId}/image/{imageId}/main")
    public ResponseEntity<?> makeProductMainImage(@PathVariable Long productId,
                                                  @PathVariable Long imageId) {
        return productService.makeProductMainImage(productId, imageId);
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/product/{productId}/image/{imageId}")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long productId,
                                                @PathVariable Long imageId) {
        return productService.deleteProductImage(productId, imageId);
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/seller/products")
    public ResponseEntity<List<SellerProductProjection>> getCurrentSellerProducts(Authentication auth) {
        String username = auth.getName();
        return productService.getCurrentSellerProducts(username);
    }


    //TODO different endpoints for different variations of products
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/products")
    public ResponseEntity<List<ProductGetDto>> getAllProducts() {
        return null;
    }
}
