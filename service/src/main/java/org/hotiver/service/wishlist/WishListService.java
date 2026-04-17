package org.hotiver.service.wishlist;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Exception.base.ResourceNotFoundException;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.product.ProductImageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class WishListService {

    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final ProductImageService productImageService;
    private final CurrentUserService currentUserService;

    public WishListService(UserRepo userRepo, ProductRepo productRepo,
                           ProductImageService productImageService,
                           CurrentUserService currentUserService) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.productImageService = productImageService;
        this.currentUserService = currentUserService;
    }

    public List<ListProductDto> getUserWishList() {
        SecurityUser user = currentUserService.getUserPrincipal();

        List<ListProductDto> products = productRepo.findUserProductWishList(user.getUsername());

        productImageService.addHostToImage(products);

        return products;
    }

    public void removeProductFromWishList(Long productId) {
        User user = currentUserService.getCurrentUser();

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        user.getWishlist().remove(product);
        userRepo.save(user);
    }

    public void addProductInWishList(Long productId) {
        User user = currentUserService.getCurrentUser();

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (user.getWishlist().contains(product)){
            return;
        }
        if (user.getId().equals(product.getSeller().getId())) {
            return;
        }
        Set<Product> products = user.getWishlist();
        products.add(product);
        user.setWishlist(products);
        userRepo.save(user);
    }
}
