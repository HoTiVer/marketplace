package org.hotiver.dto.validation.Promotion;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hotiver.dto.product.ProductPromotionRequest;

public class PromotionTimeValidator
        implements ConstraintValidator<ValidPromotionTime, ProductPromotionRequest> {
    @Override
    public boolean isValid(ProductPromotionRequest dto,
                           ConstraintValidatorContext constraintValidatorContext) {

        if (dto.startTime() ==  null || dto.endTime() == null) {
            return true;
        }

        return dto.startTime().isBefore(dto.endTime());
    }
}
