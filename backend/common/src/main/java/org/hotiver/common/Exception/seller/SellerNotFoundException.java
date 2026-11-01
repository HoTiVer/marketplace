package org.hotiver.common.Exception.seller;

import org.hotiver.common.Exception.user.UserNotFoundException;

public class SellerNotFoundException extends UserNotFoundException {
    public SellerNotFoundException(String message) {
        super(message);
    }
}
