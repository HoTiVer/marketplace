package org.hotiver.dto.product;

import java.math.BigDecimal;
import java.util.Map;

public interface ProductProjection {
    Long getProductId();
    String getProductName();
    BigDecimal getPrice();
    String getDescription();
    String getCategoryName();
    String getMainImageUrl();
}
