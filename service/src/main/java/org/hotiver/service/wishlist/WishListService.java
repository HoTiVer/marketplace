package org.hotiver.service.wishlist;

import org.hotiver.common.Exception.base.ResourceNotFoundException;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.product.ProductImageService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WishListService {

    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final ProductImageService productImageService;

    public WishListService(UserRepo userRepo, ProductRepo productRepo,
                           ProductImageService productImageService) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.productImageService = productImageService;
    }

    public List<ListProductDto> getUserWishList() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        List<ListProductDto> products = productRepo.findUserProductWishList(email);

        productImageService.addHostToImage(products);

        return products;
    }

    public void removeProductFromWishList(Long productId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepo.findByEmail(email).get();
        Optional<Product> product = productRepo.findById(productId);

        if (product.isPresent()) {
            user.getWishlist().remove(product.get());
            userRepo.save(user);
        }
    }

    public void addProductInWishList(Long productId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepo.findByEmail(email).get();

        Optional<Product> product = productRepo.findById(productId);

        if (product.isEmpty()){
            throw new ResourceNotFoundException("Product not found");
        }
        if (user.getWishlist().contains(product.get())){
            return;
        }
        if (user.getId().equals(product.get().getSeller().getId())) {
            return;
        }
        var products = user.getWishlist();
        products.add(product.get());
        user.setWishlist(products);
        userRepo.save(user);
    }
}
