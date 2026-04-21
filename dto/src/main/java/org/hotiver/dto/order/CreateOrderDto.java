package org.hotiver.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateOrderDto(
        @NotBlank(message = "Address cannot be blank")
        String deliveryAddress,

        @NotBlank(message = "City cannot be blank")
        String deliveryCity,

        @NotBlank(message = "You must input a name of receiver")
        String receiverName,

        @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{10,15}$",
                message = "Invalid phone number. Example: +380991234567")
        String receiverPhone
) {}
