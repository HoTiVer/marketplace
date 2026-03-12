package org.hotiver.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateOrderDto(
        @NotBlank
        String deliveryAddress,

        @NotBlank
        String deliveryCity,

        @NotBlank
        String receiverName,

        @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
        String receiverPhone
) {}
