package org.hotiver.service;

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class WishListService {

    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    public WishListService(UserRepo userRepo, ProductRepo productRepo) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    public ResponseEntity<List<ListProductDto>> getUserWishList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepo.findByEmail(email).get();

        var userWishList = user.getWishlist();

        if (userWishList.isEmpty()){
            return ResponseEntity.ok().body(Collections.emptyList());
        }

        List<ListProductDto> wishListProducts = new ArrayList<>();

        ListProductDto listProductDto;
        for (var product : userWishList){
            listProductDto = new ListProductDto(
                    product.getId(),
                    product.getName(),
                    product.getPrice()
            );
            wishListProducts.add(listProductDto);
        }

        return ResponseEntity.ok().body(wishListProducts);
    }

    public void removeProductFromWishList(Long productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> opUser = userRepo.findByEmail(email);
        if (opUser.isEmpty()){
            return;
        }

        User user = opUser.get();
        Optional<Product> product = productRepo.findById(productId);

        if (product.isPresent()) {
            user.getWishlist().remove(product.get());
            userRepo.save(user);
        }
    }

    public void addProductInWishList(Long productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> opUser = userRepo.findByEmail(email);
        if (opUser.isEmpty()){
            return;
        }

        User user = opUser.get();
        Optional<Product> product = productRepo.findById(productId);

        if (product.isEmpty()){
            return;
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
