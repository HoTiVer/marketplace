package org.hotiver.service.mapper;

import org.hotiver.domain.Entity.ProductPromotion;
import org.hotiver.dto.product.ProductPromotionRequest;
import org.hotiver.dto.product.SellerProductPromotionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductPromotionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    void updateProductFromDto(ProductPromotionRequest dto, @MappingTarget ProductPromotion entity);

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    @Mapping(source = "id", target = "promotionId")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "discountPercent", target = "discountPercent")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(source = "active", target = "active")
    @Mapping(source = "showEndDate", target = "showEndDate")
    SellerProductPromotionDto entityToSellerProductPromotionDto(ProductPromotion entity);

    default OffsetDateTime map(Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
