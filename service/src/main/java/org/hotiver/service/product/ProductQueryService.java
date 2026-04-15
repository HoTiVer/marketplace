package org.hotiver.service.product;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Exception.seller.SellerNotFoundException;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.dto.product.*;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.service.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductQueryService {

    private final ProductRepo productRepo;
    private final ProductMapper productMapper;
    private final ProductImageService productImageService;
    private final SellerRepo sellerRepo;

    public ProductQueryService(ProductRepo productRepo, ProductMapper productMapper,
                               ProductImageService productImageService, SellerRepo sellerRepo) {
        this.productRepo = productRepo;
        this.productMapper = productMapper;
        this.productImageService = productImageService;
        this.sellerRepo = sellerRepo;
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

        List<SellerInventoryProductDto> sellerProducts = productRepo
                .getCurrentSellerProducts(seller.getId());

        productImageService.setSellerInventoryProductDtoImageUrl(sellerProducts);

        return sellerProducts;
    }

    public CurrentSellerProductDto getCurrentSellerProductById(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(()-> new EntityNotFoundException("Product with id "
                        + productId + " not found"));

        CurrentSellerProductDto returnProduct = productMapper.entityToCurrentSellerProductDto(
                product
        );

        List<ProductImageDto> images = productImageService.getProductImagesDto(product);
        returnProduct.setImages(images);

        return returnProduct;
    }

    public List<ListProductDto> getSellerProducts(String username) {
        Seller seller = sellerRepo.findByNickname(username)
                .orElseThrow(()-> new SellerNotFoundException("Seller not found"));

        return productRepo.findAllVisibleBySellerId(seller.getId());
    }

}
