package org.hotiver.dto.seller;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SellerRegisterDto {

    String requestedNickname;
    String displayName;
    String description;
}
