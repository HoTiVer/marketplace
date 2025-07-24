package org.hotiver.dto.seller;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SellerProfileDto {
    private Long id;
    private String displayName;
    private String nickname;
    private Double rating;
    private String profileDescription;

}
