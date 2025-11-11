package org.hotiver.api;

import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.dto.product.ProductProjection;
import org.hotiver.service.ProductSearchService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/search")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    public ProductSearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @GetMapping("/product")
    public ResponseEntity<Page<ProductGetDto>> productSearchByKeyWords(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productSearchService.productSearchByKeyWords(searchTerm, page, size);
    }

    @GetMapping("/product/category/{category}")
    public ResponseEntity<Page<ProductProjection>> productSearchByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productSearchService.productSearchByCategory(category, page, size);
    }
}
