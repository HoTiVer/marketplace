package org.hotiver.dto.seller;

import java.util.Map;

public interface SellerProductProjection {
    Long getId();
    String getName();
    Double getPrice();
    String getDescription();
    String getCategoryName();
    Map<String, Object> getCharacteristic();
    String getSellerDisplayName();
    String getSellerUsername();
    Integer getQuantity();
}
