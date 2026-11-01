package org.hotiver.dto.validation.RegisterRequest;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = StrongPasswordValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "weak password";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
