package org.hotiver.service.mapper;

import org.hotiver.domain.Entity.Category;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.dto.product.ProductAddDto;
import org.hotiver.dto.product.ProductGetDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductMapper {

    @Mapping(source = "quantity", target = "stockQuantity")
    @Mapping(source = "characteristics", target = "characteristic")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "salesCount", ignore = true)
    @Mapping(target = "publishingDate", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "isVisible", ignore = true)
    @Mapping(target = "images", ignore = true)
    void updateProductFromDto(ProductAddDto productAddDto, @MappingTarget Product product);


    @Mapping(source = "productAddDto.quantity", target = "stockQuantity")
    @Mapping(source = "productAddDto.characteristics", target = "characteristic")
    @Mapping(source = "productAddDto.name", target = "name")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "seller", source = "seller")
    @Mapping(target = "salesCount", constant = "0")
    @Mapping(target = "publishingDate", expression = "java(java.sql.Date.valueOf(java.time.LocalDate.now()))")
    @Mapping(target = "rating", expression = "java(java.math.BigDecimal.valueOf(0.0))")
    @Mapping(target = "isVisible", constant = "true")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "images", ignore = true)
    Product productAddDtoToEntity(ProductAddDto productAddDto, Category category, Seller seller);


    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "seller.user.displayName", target = "sellerDisplayName")
    @Mapping(source = "seller.nickname", target = "sellerUsername")
    @Mapping(source = "product.id", target = "id")
    @Mapping(source = "product.name", target = "name")
    ProductGetDto entityToProductGetDto(Product product, Category category, Seller seller);
}
