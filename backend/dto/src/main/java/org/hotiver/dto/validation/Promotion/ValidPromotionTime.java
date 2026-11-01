package org.hotiver.dto.validation.Promotion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Constraint(validatedBy = PromotionTimeValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPromotionTime {
    String message() default "Promotion start time must be before end time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
