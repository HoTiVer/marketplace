package org.hotiver.dto.validation.RegisterRequest;

public interface EmailUniqueChecker {
    boolean isUnique(String email);
}
