package org.hotiver.service;

import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.dto.product.SellerProductDto;
import org.hotiver.dto.seller.SellerProfileDto;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.SellerRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SellerService {

    private final SellerRepo sellerRepo;
    private final ProductRepo productRepo;

    public SellerService(SellerRepo sellerRepo, ProductRepo productRepo) {
        this.sellerRepo = sellerRepo;
        this.productRepo = productRepo;
    }

    public ResponseEntity<SellerProfileDto> getSellerByUsername(String username) {
        Optional<Seller> opSeller = sellerRepo.findByUsername(username);

        if (opSeller.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        var seller = opSeller.get();

        SellerProfileDto sellerProfileDto = SellerProfileDto.builder()
                .id(seller.getId())
                .displayName(seller.getUser().getDisplayName())
                .nickname(seller.getNickname())
                .rating(seller.getRating())
                .profileDescription(seller.getProfileDescription())
                .build();

        return ResponseEntity.ok().body(sellerProfileDto);
    }

    public ResponseEntity<List<SellerProductDto>> getSellerProducts(String username) {
        Optional<Seller> opSeller = sellerRepo.findByUsername(username);

        if (opSeller.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        List<SellerProductDto> returnProducts = new ArrayList<>();
        List<Product> sellerProducts = productRepo.findAllBySellerId(opSeller.get().getId());

        for (var product : sellerProducts) {
            SellerProductDto sellerProductDto = new SellerProductDto(
                    product.getName(),
                    product.getPrice());

            returnProducts.add(sellerProductDto);
        }

        return ResponseEntity.ok().body(returnProducts);
    }
}
