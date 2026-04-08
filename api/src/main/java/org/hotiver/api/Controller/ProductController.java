package org.hotiver.api.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.hotiver.dto.product.CurrentSellerProductDto;
import org.hotiver.dto.product.ProductAddDto;
import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.dto.seller.SellerProductProjection;
import org.hotiver.service.product.ProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductGetDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok().body(productService.getProductById(id));
    }

    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Add product")
    @Parameters(value = {
            @Parameter(
                    name = "data",
                    description = "Product data (JSON)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductAddDto.class)
                    )
            ),
            @Parameter(
                    name = "image",
                    description = "Product image",
                    required = false,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
    })
    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(
            @Valid @RequestPart("data") ProductAddDto productAddDto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        productService.addProduct(productAddDto, image);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @DeleteMapping("/product/{id}")
    public ResponseEntity<?> deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Update product")
    @Parameters(value = {
            @Parameter(
                    name = "data",
                    description = "Product data (JSON)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductAddDto.class)
                    )
            ),
            @Parameter(
                    name = "image",
                    description = "Product image",
                    required = false,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
    })
    @PutMapping(value = "/product/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProductById(
            @PathVariable Long id,
            @RequestPart("data") @Valid ProductAddDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        productService.updateProductById(id, dto, image);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('SELLER')")
    @PatchMapping("/product/{productId}/image/{imageId}/main")
    public ResponseEntity<Void> makeProductMainImage(@PathVariable Long productId,
                                                  @PathVariable Long imageId) {
        productService.makeProductMainImage(productId, imageId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/product/{productId}/image/{imageId}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable Long productId,
                                                @PathVariable Long imageId) {
        productService.deleteProductImage(productId, imageId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/seller/products")
    public ResponseEntity<List<SellerProductProjection>> getCurrentSellerProducts(Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok()
                .body(productService.getCurrentSellerProducts(username));
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/seller/products/{id}")
    public ResponseEntity<CurrentSellerProductDto> getCurrentSellerProductById(
            @PathVariable Long id) {
        return ResponseEntity.ok()
                .body(productService.getCurrentSellerProductById(id));
    }


    //TODO different endpoints for different variations of products
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/products")
    public ResponseEntity<List<ProductGetDto>> getAllProducts() {
        return null;
    }
}
