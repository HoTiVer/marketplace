package org.hotiver.dto.product;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductImageDto {
    private Long id;
    private String url;
    private Boolean isMain;
}
