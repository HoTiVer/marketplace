package org.hotiver.dto.admin;

public record SellerRegisterResponse (
        Long id,
        String requestedNickname,
        String displayName,
        String profileDescription
) { }
