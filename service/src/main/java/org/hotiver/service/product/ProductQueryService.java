package org.hotiver.service.product;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Exception.auth.ForbiddenOperationException;
import org.hotiver.common.Exception.seller.SellerNotFoundException;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.product.*;
import org.hotiver.repo.core.ProductRepo;
import org.hotiver.repo.core.SellerRepo;
import org.hotiver.repo.projection.ProductProjectionRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.mapper.ProductMapper;
import org.hotiver.service.storage.ImageStorageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductQueryService {

    private final ProductRepo productRepo;
    private final ProductMapper productMapper;
    private final ProductImageService productImageService;
    private final SellerRepo sellerRepo;
    private final CurrentUserService currentUserService;
    private final ProductProjectionRepo productProjectionRepo;
    private final ImageStorageService imageStorageService;

    public ProductQueryService(ProductRepo productRepo, ProductMapper productMapper,
                               ProductImageService productImageService, SellerRepo sellerRepo,
                               CurrentUserService currentUserService,
                               ProductProjectionRepo productProjectionRepo, ImageStorageService imageStorageService) {
        this.productRepo = productRepo;
        this.productMapper = productMapper;
        this.productImageService = productImageService;
        this.sellerRepo = sellerRepo;
        this.currentUserService = currentUserService;
        this.productProjectionRepo = productProjectionRepo;
        this.imageStorageService = imageStorageService;
    }

    public ProductGetDto getProductById(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Product with id " +
                        id + " not found"));

        ProductGetDto returnProduct = productMapper.entityToProductGetDto(
                product
        );

        List<ProductImageDto> images = productImageService.getProductImagesDto(product);
        returnProduct.setImages(images);

        return returnProduct;
    }

    public List<SellerInventoryProductDto> getCurrentSellerProducts(String username) {
        Seller seller = sellerRepo.findByEmail(username)
                .orElseThrow(()-> new EntityNotFoundException("Seller does not exist"));

        List<SellerInventoryProductDto> sellerProducts = productProjectionRepo
                .getCurrentSellerProducts(seller.getId());

        productImageService.setSellerInventoryProductDtoImageUrl(sellerProducts);

        return sellerProducts;
    }

    public CurrentSellerProductDto getCurrentSellerProductById(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(()-> new EntityNotFoundException("Product with id "
                        + productId + " not found"));

        isSellerOwnProduct(product);

        CurrentSellerProductDto returnProduct = productMapper.entityToCurrentSellerProductDto(
                product
        );

        List<ProductImageDto> images = productImageService.getProductImagesDto(product);
        returnProduct.setImages(images);

        return returnProduct;
    }

    public List<ListProductDto> getSellerVisibleProducts(String username) {
        Seller seller = sellerRepo.findByNickname(username)
                .orElseThrow(()-> new SellerNotFoundException("Seller not found"));

        var products = productProjectionRepo.findAllVisibleBySellerId(seller.getId());
        productImageService.addHostToImage(products);
        return products;
    }

    private void isSellerOwnProduct(Product product) {
        SecurityUser user = currentUserService.getUserPrincipal();
        if (!user.getId().equals(product.getSeller().getId())) {
            throw new ForbiddenOperationException("Seller is not own product");
        }
    }

}
