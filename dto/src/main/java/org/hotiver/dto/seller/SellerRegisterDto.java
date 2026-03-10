package org.hotiver.dto.seller;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SellerRegisterDto {
    @NotBlank(message = "Nickname must be assigned")
    String requestedNickname;
    @NotBlank(message = "Display name must be assigned")
    String displayName;

    String description;
}
