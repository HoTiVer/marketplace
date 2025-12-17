package org.hotiver.dto.product;

import java.util.Map;

public interface ProductProjection {
    Long getId();
    String getName();
    Double getPrice();
    String getDescription();
    String getCategoryName();
    String getImageUrl();
}
