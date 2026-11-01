package org.hotiver.dto.validation.RegisterRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DuplicatedEmailValidator
        implements ConstraintValidator<DuplicatedEmailConstraint, String> {

    @Autowired(required = false)
    private EmailUniqueChecker emailChecker;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return emailChecker.isUnique(value);
    }
}