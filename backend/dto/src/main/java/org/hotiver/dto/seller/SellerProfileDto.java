package org.hotiver.dto.seller;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class SellerProfileDto {
    private Long id;
    private String displayName;
    private String nickname;
    private BigDecimal rating;
    private String profileDescription;

}
