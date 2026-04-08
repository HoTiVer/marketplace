package org.hotiver.service.user;

import org.hotiver.common.Exception.seller.SellerNotFoundException;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.dto.chat.SendMessageDto;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.dto.seller.SellerProfileDto;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.service.chat.ChatService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerService {

    private final SellerRepo sellerRepo;
    private final ProductRepo productRepo;
    private final ChatService chatService;

    public SellerService(SellerRepo sellerRepo, ProductRepo productRepo,
                         ChatService chatService) {
        this.sellerRepo = sellerRepo;
        this.productRepo = productRepo;
        this.chatService = chatService;
    }

    public SellerProfileDto getSellerByUsername(String username) {
        Seller seller = sellerRepo.findByNickname(username)
                .orElseThrow(()-> new SellerNotFoundException("Seller not found"));

        return SellerProfileDto.builder()
                .id(seller.getId())
                .displayName(seller.getUser().getDisplayName())
                .nickname(seller.getNickname())
                .rating(seller.getRating())
                .profileDescription(seller.getProfileDescription())
                .build();
    }

    public List<ListProductDto> getSellerProducts(String username) {
        Seller seller = sellerRepo.findByNickname(username)
                .orElseThrow(()-> new SellerNotFoundException("Seller not found"));

        return productRepo.findAllVisibleBySellerId(seller.getId());
    }

    public void sendMessageToSeller(String username, SendMessageDto message) {
        chatService.sendMessageToSeller(username, message);
    }
}


